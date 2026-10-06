package com.etch.notificationservice.web;

import com.etch.dto.NotificationResponse;
import com.etch.notificationservice.domain.Notification;

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
}
