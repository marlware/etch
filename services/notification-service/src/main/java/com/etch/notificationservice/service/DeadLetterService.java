package com.etch.notificationservice.service;

import com.etch.events.NotificationFailedEvent;
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
 * Single path for permanently failing a notification, whether it got
 * there via a non-retryable error on the first attempt or by exhausting
 * every retry. Persists the terminal state, appends an audit entry, and
 * publishes both {@code notification-failed} (for anything else that
 * cares) and {@code notification-dlt} (archived by
 * {@link com.etch.notificationservice.kafka.NotificationDltConsumer} for
 * {@code GET /admin/dlt}).
 */
@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationAuditRepository auditRepository;
    private final NotificationEventProducer eventProducer;
    private final NotificationMetrics metrics;

    public DeadLetterService(NotificationRepository notificationRepository,
                              NotificationAuditRepository auditRepository,
                              NotificationEventProducer eventProducer,
                              NotificationMetrics metrics) {
        this.notificationRepository = notificationRepository;
        this.auditRepository = auditRepository;
        this.eventProducer = eventProducer;
        this.metrics = metrics;
    }

    @Transactional
    public void deadLetter(Notification notification, String reason, String correlationId) {
        notification.markDeadLettered();
        notificationRepository.save(notification);
        auditRepository.save(new NotificationAudit(notification.getId(), "DEAD_LETTERED", reason));

        log.warn("Notification {} (order {}, channel {}) dead-lettered after {} attempts: {}",
                notification.getId(), notification.getOrderId(), notification.getChannel(),
                notification.getRetryCount(), reason);
        metrics.incrementDeadLettered();

        NotificationFailedEvent event = new NotificationFailedEvent(
                correlationId,
                notification.getId(),
                notification.getOrderId(),
                notification.getChannel(),
                reason,
                notification.getRetryCount()
        );
        eventProducer.publishFailed(event);
        eventProducer.publishToDlt(event);
    }
}
