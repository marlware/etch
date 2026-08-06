package com.etch.notificationservice.domain;

import com.etch.events.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Durable copy of everything published to the {@code notification-dlt}
 * topic, written by {@link com.etch.notificationservice.kafka.NotificationDltConsumer}
 * so that {@code GET /admin/dlt} has something to query -- Kafka topics
 * themselves aren't queryable over REST.
 */
@Entity
@Table(name = "notification_dlt")
public class NotificationDlt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Lob
    @Column(name = "original_payload")
    private String originalPayload;

    @Column(name = "failure_reason", nullable = false, length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt = Instant.now();

    protected NotificationDlt() {
    }

    public NotificationDlt(Long notificationId, Long orderId, NotificationChannel channel,
                            String originalPayload, String failureReason, int retryCount) {
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.channel = channel;
        this.originalPayload = originalPayload;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
    }

    public Long getId() {
        return id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getFailedAt() {
        return failedAt;
    }
}
