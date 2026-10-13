package com.etch.notificationservice.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExponentialBackOff implements BackOff {

    private static final long MAX_DELAY_MS = 30_000L;

    private final long baseDelayMs;

    public ExponentialBackOff(@Value("${etch.retry.base-delay-ms:500}") long baseDelayMs) {
        this.baseDelayMs = baseDelayMs;
    }

    @Override
    public void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(delayMsFor(attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    long delayMsFor(int attempt) {
        // clamp the shift so a large attempt count can't overflow into a negative delay
        int shift = Math.min(Math.max(0, attempt - 1), 20);
        return Math.min(baseDelayMs << shift, MAX_DELAY_MS);
    }
}
