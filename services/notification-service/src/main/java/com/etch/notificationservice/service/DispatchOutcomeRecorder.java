package com.etch.notificationservice.service;

import com.etch.common.exception.NotificationException;
import com.etch.dto.SendMessageResponse;
import com.etch.events.NotificationRequestedEvent;
import com.etch.events.NotificationSentEvent;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationAudit;
import com.etch.notificationservice.domain.NotificationAuditRepository;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.kafka.NotificationEventProducer;
import com.etch.notificationservice.metrics.NotificationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists the outcome of a single dispatch attempt. Kept as its own bean
 * (rather than private methods on {@link NotificationDispatchService}) so
 * {@code @Transactional} is applied through a real proxy call instead of
 * a same-class self-invocation, which Spring's proxy-based AOP would
 * silently skip.
 */
@Service
class DispatchOutcomeRecorder {

    private static final Logger log = LoggerFactory.getLogger(DispatchOutcomeRecorder.class);

    private final NotificationRepository notificationRepository;
    private final NotificationAuditRepository auditRepository;
    private final NotificationEventProducer eventProducer;
    private final NotificationMetrics metrics;

    DispatchOutcomeRecorder(NotificationRepository notificationRepository,
                             NotificationAuditRepository auditRepository,
                             NotificationEventProducer eventProducer,
                             NotificationMetrics metrics) {
        this.notificationRepository = notificationRepository;
        this.auditRepository = auditRepository;
        this.eventProducer = eventProducer;
        this.metrics = metrics;
    }

    @Transactional
    void recordSuccess(Notification notification, NotificationRequestedEvent requested, SendMessageResponse response) {
        notification.markSent();
        notificationRepository.save(notification);
        auditRepository.save(new NotificationAudit(notification.getId(), "SENT",
                "Delivered via " + notification.getChannel() + " (provider id " + response.providerMessageId() + ")"));

        log.info("Notification {} (order {}, channel {}) sent", notification.getId(), notification.getOrderId(), notification.getChannel());
        metrics.incrementSent();

        eventProducer.publishSent(new NotificationSentEvent(
                requested.getCorrelationId(), notification.getId(), notification.getOrderId(), notification.getChannel()));
    }

    @Transactional
    void recordFailedAttempt(Notification notification, int attempt, int maxAttempts, NotificationException ex) {
        notification.recordFailedAttempt();
        notificationRepository.save(notification);
        auditRepository.save(new NotificationAudit(notification.getId(), "ATTEMPT_FAILED",
                "Attempt " + attempt + "/" + maxAttempts + " failed (retryable=" + ex.isRetryable() + "): " + ex.getMessage()));
        log.warn("Notification {} dispatch attempt {}/{} failed: {}", notification.getId(), attempt, maxAttempts, ex.getMessage());
        metrics.incrementFailedAttempt();
    }
}
