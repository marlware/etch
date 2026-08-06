package com.etch.notificationservice.service;

import com.etch.common.exception.NotificationException;
import com.etch.dto.SendMessageResponse;
import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import com.etch.notificationservice.client.ChannelClient;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.metrics.NotificationMetrics;
import com.etch.notificationservice.retry.BackOff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    private static final int MAX_ATTEMPTS = 3;

    @Mock
    private ChannelClient emailClient;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private DeadLetterService deadLetterService;
    @Mock
    private DispatchOutcomeRecorder outcomeRecorder;
    @Mock
    private NotificationMetrics metrics;
    @Mock
    private BackOff backOff;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        when(emailClient.channel()).thenReturn(NotificationChannel.EMAIL);
        dispatchService = new NotificationDispatchService(
                List.of(emailClient), notificationRepository, deadLetterService, outcomeRecorder, metrics, backOff, MAX_ATTEMPTS);
    }

    private Notification pendingNotification() {
        Notification notification = new Notification(1L, NotificationChannel.EMAIL, "a@example.com", "corr-1");
        setId(notification, 10L);
        return notification;
    }

    private static void setId(Notification notification, long id) {
        try {
            Field field = Notification.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(notification, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void dispatch_succeedsOnFirstAttempt() {
        Notification notification = pendingNotification();
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(emailClient.send(anyString(), anyString(), anyString())).thenReturn(SendMessageResponse.success("provider-1"));

        dispatchService.dispatch(event);

        verify(outcomeRecorder).recordSuccess(eq(notification), eq(event), any());
        verify(deadLetterService, never()).deadLetter(any(), anyString(), anyString());
        verifyNoInteractions(backOff);
    }

    @Test
    void dispatch_retriesTransientFailuresThenSucceeds() {
        Notification notification = pendingNotification();
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(emailClient.send(anyString(), anyString(), anyString()))
                .thenThrow(new NotificationException("EMAIL service is unavailable", true))
                .thenThrow(new NotificationException("EMAIL service is unavailable", true))
                .thenReturn(SendMessageResponse.success("provider-1"));

        dispatchService.dispatch(event);

        verify(outcomeRecorder, times(2)).recordFailedAttempt(eq(notification), anyInt(), eq(MAX_ATTEMPTS), any());
        verify(backOff, times(2)).waitBeforeRetry(anyInt());
        verify(outcomeRecorder).recordSuccess(eq(notification), eq(event), any());
        verify(deadLetterService, never()).deadLetter(any(), anyString(), anyString());
    }

    @Test
    void dispatch_deadLettersAfterExhaustingRetries() {
        Notification notification = pendingNotification();
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(emailClient.send(anyString(), anyString(), anyString()))
                .thenThrow(new NotificationException("EMAIL service is unavailable", true));

        dispatchService.dispatch(event);

        verify(outcomeRecorder, times(MAX_ATTEMPTS)).recordFailedAttempt(eq(notification), anyInt(), eq(MAX_ATTEMPTS), any());
        // no backoff wait before the final attempt -- it goes straight to the dead letter
        verify(backOff, times(MAX_ATTEMPTS - 1)).waitBeforeRetry(anyInt());
        verify(deadLetterService).deadLetter(eq(notification), anyString(), eq("corr-1"));
        verify(outcomeRecorder, never()).recordSuccess(any(), any(), any());
    }

    @Test
    void dispatch_deadLettersImmediatelyOnNonRetryableFailure() {
        Notification notification = pendingNotification();
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(emailClient.send(anyString(), anyString(), anyString()))
                .thenThrow(new NotificationException("EMAIL service rejected the request", false));

        dispatchService.dispatch(event);

        verify(outcomeRecorder, times(1)).recordFailedAttempt(eq(notification), eq(1), eq(MAX_ATTEMPTS), any());
        verifyNoInteractions(backOff);
        verify(deadLetterService).deadLetter(eq(notification), anyString(), eq("corr-1"));
    }

    @Test
    void dispatch_skipsWhenNotificationAlreadySent() {
        Notification notification = pendingNotification();
        notification.markSent();
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 10L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        dispatchService.dispatch(event);

        // emailClient.channel() is invoked once up front while the
        // dispatcher builds its channel->client map, so we assert on the
        // absence of a dispatch attempt rather than zero interactions
        verify(emailClient, never()).send(anyString(), anyString(), anyString());
        verifyNoInteractions(deadLetterService, outcomeRecorder, backOff);
    }

    @Test
    void dispatch_ignoresUnknownNotification() {
        NotificationRequestedEvent event = new NotificationRequestedEvent("corr-1", 999L, 1L, NotificationChannel.EMAIL, "a@example.com");
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        dispatchService.dispatch(event);

        verify(emailClient, never()).send(anyString(), anyString(), anyString());
        verifyNoInteractions(deadLetterService, outcomeRecorder, backOff);
    }
}
