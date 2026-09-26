package com.etch.common.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void validationExceptionWithoutDetailsHasAnEmptyDetailList() {
        assertThat(new ValidationException("bad").getDetails()).isEmpty();
    }

    @Test
    void validationExceptionKeepsItsDetails() {
        ValidationException ex = new ValidationException("bad", List.of("a: required", "b: too long"));

        assertThat(ex.getDetails()).containsExactly("a: required", "b: too long");
    }

    @Test
    void notificationExceptionReportsWhetherItIsRetryable() {
        assertThat(new NotificationException("timeout", true).isRetryable()).isTrue();
        assertThat(new NotificationException("bad payload", false).isRetryable()).isFalse();
    }

    @Test
    void notificationExceptionPreservesItsCause() {
        Throwable cause = new IllegalStateException("socket closed");

        NotificationException ex = new NotificationException("dispatch failed", true, cause);

        assertThat(ex).hasCause(cause);
    }

    @Test
    void kafkaExceptionPreservesItsCause() {
        Throwable cause = new RuntimeException("broker unreachable");

        assertThat(new KafkaException("publish failed", cause)).hasCause(cause);
    }
}
