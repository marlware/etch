package com.etch.notificationservice.retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExponentialBackOff implements BackOff {

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
        return baseDelayMs * (1L << Math.max(0, attempt - 1));
    }
}
