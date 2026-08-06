package com.etch.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank(message = "recipient is required")
        String recipient,

        String subject,

        @NotBlank(message = "body is required")
        String body,

        String correlationId
) {
}
