package com.etch.dto;

import com.etch.events.NotificationChannel;

import java.time.Instant;

public record DltRecordResponse(
        Long id,
        Long notificationId,
        Long orderId,
        NotificationChannel channel,
        String originalPayload,
        String failureReason,
        int retryCount,
        Instant failedAt
) {
}
