package com.etch.dto;

public record SendMessageResponse(
        boolean success,
        String providerMessageId,
        String failureReason
) {
    public static SendMessageResponse success(String providerMessageId) {
        return new SendMessageResponse(true, providerMessageId, null);
    }

    public static SendMessageResponse failure(String reason) {
        return new SendMessageResponse(false, null, reason);
    }
}
