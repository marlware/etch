package com.etch.notificationservice.kafka;

import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import com.etch.notificationservice.redis.IdempotencyService;
import com.etch.notificationservice.service.NotificationDispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRequestedConsumerTest {

    @Mock
    private NotificationDispatchService dispatchService;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private Acknowledgment acknowledgment;

    private NotificationRequestedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new NotificationRequestedConsumer(dispatchService, idempotencyService);
    }

    private NotificationRequestedEvent sampleEvent() {
        return new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
    }

    @Test
    void onNotificationRequested_dispatchesAndAcknowledgesWhenLockAcquired() {
        NotificationRequestedEvent event = sampleEvent();
        when(idempotencyService.claim(anyString(), any(Duration.class))).thenReturn(true);

        consumer.onNotificationRequested(event, acknowledgment);

        verify(dispatchService).dispatch(event);
        verify(acknowledgment).acknowledge();
        verify(idempotencyService).release(anyString());
    }

    @Test
    void onNotificationRequested_skipsDispatchWhenAlreadyInFlight() {
        NotificationRequestedEvent event = sampleEvent();
        when(idempotencyService.claim(anyString(), any(Duration.class))).thenReturn(false);

        consumer.onNotificationRequested(event, acknowledgment);

        verify(dispatchService, never()).dispatch(any());
        verify(acknowledgment).acknowledge();
    }
}
