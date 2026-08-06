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
        long delayMs = baseDelayMs * (1L << Math.max(0, attempt - 1));
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
