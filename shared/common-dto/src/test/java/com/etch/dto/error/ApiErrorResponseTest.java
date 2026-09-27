package com.etch.dto.error;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    @Test
    void shortConstructorDefaultsToNoDetailsAndStampsTheCurrentTime() {
        Instant before = Instant.now();

        ApiErrorResponse response = new ApiErrorResponse(404, "NOT_FOUND", "missing", "/orders/9", "corr-1");

        assertThat(response.details()).isEmpty();
        assertThat(response.timestamp()).isBetween(before, Instant.now());
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.path()).isEqualTo("/orders/9");
        assertThat(response.correlationId()).isEqualTo("corr-1");
    }

    @Test
    void detailedConstructorKeepsTheDetails() {
        ApiErrorResponse response = new ApiErrorResponse(
                400, "VALIDATION_ERROR", "bad", "/orders", null, List.of("total: must be positive"));

        assertThat(response.details()).containsExactly("total: must be positive");
        assertThat(response.correlationId()).isNull();
    }
}
