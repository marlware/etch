package com.etch.apigateway.web;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "username is required")
        String username
) {
}
