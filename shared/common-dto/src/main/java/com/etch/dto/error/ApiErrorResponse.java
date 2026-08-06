package com.etch.dto.error;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error envelope returned by every service's
 * {@code @RestControllerAdvice}.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<String> details
) {
    public ApiErrorResponse(int status, String error, String message, String path, String correlationId) {
        this(Instant.now(), status, error, message, path, correlationId, List.of());
    }

    public ApiErrorResponse(int status, String error, String message, String path, String correlationId, List<String> details) {
        this(Instant.now(), status, error, message, path, correlationId, details);
    }
}
