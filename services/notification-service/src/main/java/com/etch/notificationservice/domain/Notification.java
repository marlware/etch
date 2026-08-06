package com.etch.notificationservice.domain;

import com.etch.events.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private long version;

    protected Notification() {
    }

    public Notification(Long orderId, NotificationChannel channel, String recipient, String correlationId) {
        this.orderId = orderId;
        this.channel = channel;
        this.recipient = recipient;
        this.correlationId = correlationId;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.updatedAt = Instant.now();
    }

    public void recordFailedAttempt() {
        this.retryCount++;
        this.status = NotificationStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void markDeadLettered() {
        this.status = NotificationStatus.DEAD_LETTERED;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
