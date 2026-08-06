package com.etch.dto;

import java.time.Instant;

public record NotificationAuditEntryResponse(
        Long id,
        String event,
        String details,
        Instant timestamp
) {
}
