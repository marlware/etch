package com.etch.notificationservice.web;

import com.etch.dto.DltRecordResponse;
import com.etch.dto.NotificationAuditEntryResponse;
import com.etch.dto.NotificationResponse;
import com.etch.events.NotificationChannel;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationAudit;
import com.etch.notificationservice.domain.NotificationDlt;
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

    @Test
    void mapsAuditEntries() {
        NotificationAudit audit = new NotificationAudit(5L, "SENT", "providerMessageId=sms-1");

        NotificationAuditEntryResponse response = NotificationMapper.toResponse(audit);

        assertThat(response.event()).isEqualTo("SENT");
        assertThat(response.details()).isEqualTo("providerMessageId=sms-1");
        assertThat(response.timestamp()).isEqualTo(audit.getTimestamp());
    }

    @Test
    void mapsDeadLetterRecords() {
        NotificationDlt dlt = new NotificationDlt(
                3L, 10L, NotificationChannel.EMAIL, "{\"orderId\":10}", "Mailbox rejected the message", 3);

        DltRecordResponse response = NotificationMapper.toResponse(dlt);

        assertThat(response.notificationId()).isEqualTo(3L);
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(response.originalPayload()).isEqualTo("{\"orderId\":10}");
        assertThat(response.failureReason()).isEqualTo("Mailbox rejected the message");
        assertThat(response.retryCount()).isEqualTo(3);
        assertThat(response.failedAt()).isEqualTo(dlt.getFailedAt());
    }
}
