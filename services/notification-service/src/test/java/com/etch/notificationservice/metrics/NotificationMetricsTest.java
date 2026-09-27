package com.etch.notificationservice.metrics;

import com.etch.notificationservice.domain.NotificationDltRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class NotificationMetricsTest {

    private SimpleMeterRegistry registry;
    private NotificationDltRepository dltRepository;
    private NotificationMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        dltRepository = Mockito.mock(NotificationDltRepository.class);
        metrics = new NotificationMetrics(registry, dltRepository);
    }

    @Test
    void eachIncrementMethodBumpsItsOwnCounter() {
        metrics.incrementSent();
        metrics.incrementSent();
        metrics.incrementFailedAttempt();
        metrics.incrementRetried();
        metrics.incrementDeadLettered();

        assertThat(registry.counter("etch.notifications.sent").count()).isEqualTo(2.0);
        assertThat(registry.counter("etch.notifications.attempt.failed").count()).isEqualTo(1.0);
        assertThat(registry.counter("etch.notifications.retried").count()).isEqualTo(1.0);
        assertThat(registry.counter("etch.notifications.dead_lettered").count()).isEqualTo(1.0);
    }

    @Test
    void dltSizeGaugeReadsTheRepositoryCount() {
        when(dltRepository.count()).thenReturn(7L);

        assertThat(registry.get("etch.notifications.dlt.size").gauge().value()).isEqualTo(7.0);
    }
}
