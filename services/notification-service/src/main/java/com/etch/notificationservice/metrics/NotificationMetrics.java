package com.etch.notificationservice.metrics;

import com.etch.notificationservice.domain.NotificationDltRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Publishes the counters README's Monitoring section asks for
 * (notifications sent/failed, retry count, DLQ size) under
 * {@code /actuator/prometheus}.
 */
@Component
public class NotificationMetrics {

    private final Counter sentCounter;
    private final Counter failedAttemptCounter;
    private final Counter retriedCounter;
    private final Counter deadLetteredCounter;

    public NotificationMetrics(MeterRegistry registry, NotificationDltRepository dltRepository) {
        this.sentCounter = Counter.builder("etch.notifications.sent")
                .description("Notifications successfully delivered to a channel")
                .register(registry);
        this.failedAttemptCounter = Counter.builder("etch.notifications.attempt.failed")
                .description("Individual dispatch attempts that failed")
                .register(registry);
        this.retriedCounter = Counter.builder("etch.notifications.retried")
                .description("Dispatch attempts that were retried after a transient failure")
                .register(registry);
        this.deadLetteredCounter = Counter.builder("etch.notifications.dead_lettered")
                .description("Notifications that were sent to the dead letter queue")
                .register(registry);

        Gauge.builder("etch.notifications.dlt.size", dltRepository, NotificationDltRepository::count)
                .description("Current number of records archived from the dead letter topic")
                .register(registry);
    }

    public void incrementSent() {
        sentCounter.increment();
    }

    public void incrementFailedAttempt() {
        failedAttemptCounter.increment();
    }

    public void incrementRetried() {
        retriedCounter.increment();
    }

    public void incrementDeadLettered() {
        deadLetteredCounter.increment();
    }
}
