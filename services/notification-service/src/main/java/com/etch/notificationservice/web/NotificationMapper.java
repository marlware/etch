package com.etch.notificationservice.web;

import com.etch.dto.DltRecordResponse;
import com.etch.dto.NotificationAuditEntryResponse;
import com.etch.dto.NotificationResponse;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationAudit;
import com.etch.notificationservice.domain.NotificationDlt;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrderId(),
                notification.getChannel(),
                notification.getStatus().name(),
                notification.getRetryCount(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    public static NotificationAuditEntryResponse toResponse(NotificationAudit audit) {
        return new NotificationAuditEntryResponse(
                audit.getId(),
                audit.getEvent(),
                audit.getDetails(),
                audit.getTimestamp()
        );
    }

    public static DltRecordResponse toResponse(NotificationDlt dlt) {
        return new DltRecordResponse(
                dlt.getId(),
                dlt.getNotificationId(),
                dlt.getOrderId(),
                dlt.getChannel(),
                dlt.getOriginalPayload(),
                dlt.getFailureReason(),
                dlt.getRetryCount(),
                dlt.getFailedAt()
        );
    }
}
