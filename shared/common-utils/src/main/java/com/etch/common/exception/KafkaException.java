package com.etch.common.exception;

/**
 * Wraps failures publishing to, or consuming from, Kafka so callers don't
 * have to deal with the raw client exception types.
 */
public class KafkaException extends RuntimeException {

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaException(String message) {
        super(message);
    }
}
