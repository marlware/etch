package com.etch.notificationservice.service;

import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import com.etch.events.OrderCreatedEvent;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationAuditRepository;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.kafka.NotificationEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationIngestServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationAuditRepository auditRepository;
    @Mock
    private NotificationEventProducer eventProducer;
    @Mock
    private DeadLetterService deadLetterService;

    private NotificationIngestService ingestService;

    @BeforeEach
    void setUp() {
        ingestService = new NotificationIngestService(notificationRepository, auditRepository, eventProducer, deadLetterService);
        // not every test causes a save() (e.g. the no-channels case), so this
        // stub is lenient rather than required on every run
        lenient().when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void ingest_createsOneNotificationPerChannelAndPublishesRequested() {
        OrderCreatedEvent event = new OrderCreatedEvent("corr-1", 100L, "ORD-100", 5L,
                "buyer@example.com", "+15551234567", new BigDecimal("30.00"),
                List.of(NotificationChannel.EMAIL, NotificationChannel.SMS));

        ingestService.ingest(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        ArgumentCaptor<NotificationRequestedEvent> captor = ArgumentCaptor.forClass(NotificationRequestedEvent.class);
        verify(eventProducer, times(2)).publishRequested(captor.capture());

        List<NotificationRequestedEvent> requested = captor.getAllValues();
        assertThat(requested).extracting(NotificationRequestedEvent::getChannel)
                .containsExactlyInAnyOrder(NotificationChannel.EMAIL, NotificationChannel.SMS);
        assertThat(requested).extracting(NotificationRequestedEvent::getRecipient)
                .containsExactlyInAnyOrder("buyer@example.com", "+15551234567");
        verify(deadLetterService, never()).deadLetter(any(), anyString(), anyString());
    }

    @Test
    void ingest_deadLettersChannelWithMissingContactDetails() {
        OrderCreatedEvent event = new OrderCreatedEvent("corr-1", 101L, "ORD-101", 6L,
                null, null, new BigDecimal("15.00"), List.of(NotificationChannel.EMAIL));

        ingestService.ingest(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(eventProducer, never()).publishRequested(any());
        verify(deadLetterService).deadLetter(any(Notification.class), anyString(), org.mockito.ArgumentMatchers.eq("corr-1"));
    }

    @Test
    void ingest_doesNothingWhenNoChannelsRequested() {
        OrderCreatedEvent event = new OrderCreatedEvent("corr-1", 102L, "ORD-102", 7L,
                "buyer@example.com", "+15551234567", new BigDecimal("15.00"), List.of());

        ingestService.ingest(event);

        verify(notificationRepository, never()).save(any());
        verify(eventProducer, never()).publishRequested(any());
    }
}
