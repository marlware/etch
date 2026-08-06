package com.etch.common.exception;

/**
 * Raised when dispatching a notification to a downstream channel service
 * fails. Carries {@link #isRetryable()} so the retry strategy can tell
 * transient failures (network timeout, channel service unavailable) apart
 * from permanent ones (invalid payload, missing user).
 */
public class NotificationException extends RuntimeException {

    private final boolean retryable;

    public NotificationException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public NotificationException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
