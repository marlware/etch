package com.etch.notificationservice.web;

import com.etch.dto.NotificationResponse;
import com.etch.events.NotificationChannel;
import com.etch.notificationservice.domain.Notification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    @Test
    void mapsNotificationAndExposesStatusAsName() {
        Notification notification = new Notification(10L, NotificationChannel.SMS, "+15551234567", "corr-1");
        notification.recordFailedAttempt();

        NotificationResponse response = NotificationMapper.toResponse(notification);

        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.retryCount()).isEqualTo(1);
        assertThat(response.createdAt()).isEqualTo(notification.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(notification.getUpdatedAt());
    }
}
