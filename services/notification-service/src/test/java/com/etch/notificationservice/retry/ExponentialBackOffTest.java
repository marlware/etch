package com.etch.notificationservice.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExponentialBackOffTest {

    @Test
    void delayDoublesWithEachAttempt() {
        ExponentialBackOff backOff = new ExponentialBackOff(100L);

        assertThat(backOff.delayMsFor(1)).isEqualTo(100L);
        assertThat(backOff.delayMsFor(2)).isEqualTo(200L);
        assertThat(backOff.delayMsFor(3)).isEqualTo(400L);
    }

    @Test
    void neverProducesANegativeExponentForAttemptZeroOrBelow() {
        ExponentialBackOff backOff = new ExponentialBackOff(100L);

        assertThat(backOff.delayMsFor(0)).isEqualTo(100L);
        assertThat(backOff.delayMsFor(-5)).isEqualTo(100L);
    }

    @Test
    void delayIsCappedForHighAttemptCounts() {
        ExponentialBackOff backOff = new ExponentialBackOff(500L);

        assertThat(backOff.delayMsFor(10)).isEqualTo(30_000L);
        assertThat(backOff.delayMsFor(200)).isEqualTo(30_000L);
    }

    @Test
    void waitBeforeRetry_actuallySleepsApproximatelyTheComputedDelay() {
        ExponentialBackOff backOff = new ExponentialBackOff(20L);

        long start = System.nanoTime();
        backOff.waitBeforeRetry(1);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsedMs).isGreaterThanOrEqualTo(15L);
    }
}
