package com.etch.dto;

import com.etch.events.NotificationChannel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(

        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "orderNumber is required")
        @Pattern(regexp = "^[A-Za-z0-9_-]{4,40}$", message = "orderNumber must be 4-40 alphanumeric characters")
        String orderNumber,

        @NotNull(message = "total is required")
        @Positive(message = "total must be a positive amount")
        BigDecimal total,

        @NotEmpty(message = "at least one notification channel is required")
        List<NotificationChannel> channels
) {
}
