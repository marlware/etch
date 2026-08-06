package com.etch.dto;

import com.etch.events.NotificationChannel;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long orderId,
        NotificationChannel channel,
        String status,
        int retryCount,
        Instant createdAt,
        Instant updatedAt
) {
}
