package com.etch.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Common envelope fields carried by every event published to Kafka, so that
 * every hop in the pipeline can be traced by correlationId regardless of
 * which topic it flows through.
 */
public abstract class BaseEvent {

    private String eventId = UUID.randomUUID().toString();
    private String correlationId;
    private Instant occurredAt = Instant.now();

    protected BaseEvent() {
    }

    protected BaseEvent(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
