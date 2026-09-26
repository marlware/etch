package com.etch.notificationservice.domain;

import com.etch.events.NotificationChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private Notification newNotification() {
        return new Notification(10L, NotificationChannel.EMAIL, "buyer@example.com", "corr-1");
    }

    @Test
    void startsPendingWithNoRetries() {
        Notification notification = newNotification();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isZero();
        assertThat(notification.getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void markSentMovesToSent() {
        Notification notification = newNotification();

        notification.markSent();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void eachFailedAttemptIncrementsTheRetryCount() {
        Notification notification = newNotification();

        notification.recordFailedAttempt();
        notification.recordFailedAttempt();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getRetryCount()).isEqualTo(2);
    }

    @Test
    void markDeadLetteredKeepsTheRetryCount() {
        Notification notification = newNotification();
        notification.recordFailedAttempt();

        notification.markDeadLettered();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTERED);
        assertThat(notification.getRetryCount()).isEqualTo(1);
    }

    @Test
    void transitionsRefreshTheUpdatedTimestamp() {
        Notification notification = newNotification();

        notification.markSent();

        assertThat(notification.getUpdatedAt()).isAfterOrEqualTo(notification.getCreatedAt());
    }
}
