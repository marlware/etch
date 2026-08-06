package com.etch.dto;

import com.etch.events.NotificationChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String orderNumber,
        String status,
        BigDecimal total,
        List<NotificationChannel> channels,
        Instant createdAt
) {
}
