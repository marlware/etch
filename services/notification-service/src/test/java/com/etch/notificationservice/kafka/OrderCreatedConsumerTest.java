package com.etch.notificationservice.kafka;

import com.etch.events.NotificationChannel;
import com.etch.events.OrderCreatedEvent;
import com.etch.notificationservice.redis.IdempotencyService;
import com.etch.notificationservice.service.NotificationIngestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreatedConsumerTest {

    @Mock
    private NotificationIngestService ingestService;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private Acknowledgment acknowledgment;

    private OrderCreatedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCreatedConsumer(ingestService, idempotencyService);
    }

    private OrderCreatedEvent sampleEvent() {
        return new OrderCreatedEvent("corr-1", 1L, "ORD-1", 2L, "a@example.com", "+1555",
                new BigDecimal("10.00"), List.of(NotificationChannel.EMAIL));
    }

    @Test
    void onOrderCreated_ingestsAndAcknowledgesOnFirstDelivery() {
        OrderCreatedEvent event = sampleEvent();
        when(idempotencyService.claim(anyString())).thenReturn(true);

        consumer.onOrderCreated(event, acknowledgment);

        verify(ingestService).ingest(event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onOrderCreated_skipsIngestOnDuplicateDelivery() {
        OrderCreatedEvent event = sampleEvent();
        when(idempotencyService.claim(anyString())).thenReturn(false);

        consumer.onOrderCreated(event, acknowledgment);

        verify(ingestService, never()).ingest(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onOrderCreated_releasesClaimAndRethrowsOnFailure() {
        OrderCreatedEvent event = sampleEvent();
        when(idempotencyService.claim(anyString())).thenReturn(true);
        RuntimeException boom = new RuntimeException("db down");
        org.mockito.Mockito.doThrow(boom).when(ingestService).ingest(event);

        assertThatThrownBy(() -> consumer.onOrderCreated(event, acknowledgment)).isSameAs(boom);

        verify(idempotencyService).release(anyString());
        verify(acknowledgment, never()).acknowledge();
    }
}
