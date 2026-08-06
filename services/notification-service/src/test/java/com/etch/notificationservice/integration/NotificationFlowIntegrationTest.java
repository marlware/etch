package com.etch.notificationservice.integration;

import com.etch.dto.NotificationResponse;
import com.etch.events.NotificationChannel;
import com.etch.events.OrderCreatedEvent;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Drives the full pipeline notification-service owns, against real
 * infrastructure: publish an OrderCreatedEvent to a real Kafka broker,
 * let the service's own consumers/dispatcher/retry logic run unmodified
 * against a real MySQL-backed repository and real Redis-backed
 * idempotency, and confirm the notification ends up SENT. The Email/SMS
 * services are stood in for by a plain JDK HttpServer rather than mocked
 * inside the application, since the point is to exercise the real
 * ChannelClient -> RestClient -> HTTP hop.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationFlowIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    static HttpServer stubChannelServer;

    @BeforeAll
    static void startAll() throws IOException {
        MYSQL.start();
        KAFKA.start();
        REDIS.start();

        stubChannelServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        stubChannelServer.createContext("/email/send", exchange -> respondSuccess(exchange, "stub-email-1"));
        stubChannelServer.createContext("/sms/send", exchange -> respondSuccess(exchange, "stub-sms-1"));
        stubChannelServer.start();
    }

    @AfterAll
    static void stopAll() {
        stubChannelServer.stop(0);
        REDIS.stop();
        KAFKA.stop();
        MYSQL.stop();
    }

    private static void respondSuccess(com.sun.net.httpserver.HttpExchange exchange, String providerMessageId) throws IOException {
        String body = "{\"success\":true,\"providerMessageId\":\"" + providerMessageId + "\",\"failureReason\":null}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("etch.channel-clients.email-service-url",
                () -> "http://localhost:" + stubChannelServer.getAddress().getPort());
        registry.add("etch.channel-clients.sms-service-url",
                () -> "http://localhost:" + stubChannelServer.getAddress().getPort());
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void orderCreatedEvent_flowsThroughToASentNotification() throws Exception {
        long orderId = 555L;
        publishOrderCreated(orderId);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            ResponseEntity<NotificationResponse[]> response =
                    restTemplate.getForEntity("/notifications/order/" + orderId, NotificationResponse[].class);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody()[0].status()).isEqualTo("SENT");
            assertThat(response.getBody()[0].channel()).isEqualTo(NotificationChannel.EMAIL);
        });
    }

    private void publishOrderCreated(long orderId) throws Exception {
        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                "corr-it-flow", orderId, "ORD-IT-555", 1L, "buyer@example.com", null,
                new BigDecimal("20.00"), List.of(NotificationChannel.EMAIL));

        try (KafkaProducer<String, Object> producer = new KafkaProducer<>(producerProps)) {
            producer.send(new ProducerRecord<>("order-created", String.valueOf(orderId), event))
                    .get(10, TimeUnit.SECONDS);
        }
    }
}
