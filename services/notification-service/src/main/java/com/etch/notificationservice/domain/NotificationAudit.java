package com.etch.notificationservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Append-only trail of everything that happened to a notification
 * (received, dispatch attempts, sent, failed, dead-lettered) for audit
 * and troubleshooting purposes.
 */
@Entity
@Table(name = "notification_audit")
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(nullable = false, length = 40)
    private String event;

    @Column(name = "details")
    private String details;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    protected NotificationAudit() {
    }

    public NotificationAudit(Long notificationId, String event, String details) {
        this.notificationId = notificationId;
        this.event = event;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public String getEvent() {
        return event;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
