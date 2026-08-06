package com.etch.notificationservice.retry;

public interface BackOff {

    /**
     * Blocks the calling thread before retry attempt {@code attempt}
     * (1-based: called before attempt 2, 3, ...).
     */
    void waitBeforeRetry(int attempt);
}
