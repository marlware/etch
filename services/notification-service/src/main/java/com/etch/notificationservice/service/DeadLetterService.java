package com.etch.notificationservice.service;

import com.etch.events.NotificationFailedEvent;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.kafka.NotificationEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Single path for permanently failing a notification, whether it got
 * there via a non-retryable error on the first attempt or by exhausting
 * every retry. Persists the terminal state and
 * publishes both {@code notification-failed} and the {@code notification-dlt}
 * dead-letter topic.
 */
@Service
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationEventProducer eventProducer;

    public DeadLetterService(NotificationRepository notificationRepository,
                              NotificationEventProducer eventProducer) {
        this.notificationRepository = notificationRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public void deadLetter(Notification notification, String reason, String correlationId) {
        notification.markDeadLettered();
        notificationRepository.save(notification);

        log.warn("Notification {} (order {}, channel {}) dead-lettered after {} attempts: {}",
                notification.getId(), notification.getOrderId(), notification.getChannel(),
                notification.getRetryCount(), reason);

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
