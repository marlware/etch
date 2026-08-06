package com.etch.orderservice.kafka;

import com.etch.events.NotificationChannel;
import com.etch.events.OrderCreatedEvent;
import com.etch.orderservice.config.KafkaTopicConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void publishOrderCreated_sendsToOrderCreatedTopicWithOrderIdAsKey() {
        OrderEventProducer producer = new OrderEventProducer(kafkaTemplate);
        OrderCreatedEvent event = new OrderCreatedEvent(
                "corr-1", 42L, "ORD-42", 7L, "a@example.com", "+1555", new BigDecimal("5.00"),
                List.of(NotificationChannel.EMAIL));

        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopicConfig.ORDER_CREATED_TOPIC, 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> result = new SendResult<>(new ProducerRecord<>(KafkaTopicConfig.ORDER_CREATED_TOPIC, "42", event), metadata);
        when(kafkaTemplate.send(eq(KafkaTopicConfig.ORDER_CREATED_TOPIC), eq("42"), any()))
                .thenReturn(CompletableFuture.completedFuture(result));

        producer.publishOrderCreated(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq(KafkaTopicConfig.ORDER_CREATED_TOPIC), eq("42"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isSameAs(event);
    }
}
