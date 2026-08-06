package com.etch.orderservice.integration;

import com.etch.dto.OrderRequest;
import com.etch.dto.OrderResponse;
import com.etch.events.NotificationChannel;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises order-service against a real MySQL instance and a real
 * single-node Kafka broker (both via Testcontainers) end to end: POST an
 * order over HTTP, confirm it's persisted and returned, and confirm the
 * OrderCreatedEvent actually lands on the order-created topic --
 * something a mocked KafkaTemplate can't prove.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @BeforeAll
    static void startContainers() {
        MYSQL.start();
        KAFKA.start();
    }

    @AfterAll
    static void stopContainers() {
        KAFKA.stop();
        MYSQL.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void creatingAnOrder_persistsItAndPublishesOrderCreatedEvent() {
        // seeded by Flyway's V2__seed_demo_users.sql, which runs against the container on startup
        OrderRequest request = new OrderRequest(1L, "ORD-IT-1001", new BigDecimal("42.50"), List.of(NotificationChannel.EMAIL));

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/orders", request, OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().orderNumber()).isEqualTo("ORD-IT-1001");
        Long orderId = response.getBody().id();
        assertThat(orderId).isNotNull();

        ResponseEntity<OrderResponse> fetched = restTemplate.getForEntity("/orders/" + orderId, OrderResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().total()).isEqualByComparingTo("42.50");

        ConsumerRecord<String, String> record = consumeOneOrderCreatedRecord();
        assertThat(record.key()).isEqualTo(orderId.toString());
        assertThat(record.value()).contains("\"orderNumber\":\"ORD-IT-1001\"");
        assertThat(record.value()).contains("\"email\":\"ada.lovelace@example.com\"");
    }

    private ConsumerRecord<String, String> consumeOneOrderCreatedRecord() {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "order-it-verifier",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        try (Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of("order-created"));
            var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15));
            assertThat(records.count()).isGreaterThanOrEqualTo(1);
            return records.iterator().next();
        }
    }
}
