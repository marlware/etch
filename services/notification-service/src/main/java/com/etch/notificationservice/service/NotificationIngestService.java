package com.etch.notificationservice.service;

import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import com.etch.events.OrderCreatedEvent;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationAudit;
import com.etch.notificationservice.domain.NotificationAuditRepository;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.kafka.NotificationEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns an {@link OrderCreatedEvent} into one {@link Notification} row per
 * requested channel and kicks off dispatch for each by publishing
 * {@link NotificationRequestedEvent}. Splitting "requested" from
 * "order-created" lets the dispatch worker (which does the slow,
 * failure-prone work of calling out to Email/SMS) be retried and scaled
 * independently of ingestion.
 */
@Service
public class NotificationIngestService {

    private static final Logger log = LoggerFactory.getLogger(NotificationIngestService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationAuditRepository auditRepository;
    private final NotificationEventProducer eventProducer;
    private final DeadLetterService deadLetterService;

    public NotificationIngestService(NotificationRepository notificationRepository,
                                      NotificationAuditRepository auditRepository,
                                      NotificationEventProducer eventProducer,
                                      DeadLetterService deadLetterService) {
        this.notificationRepository = notificationRepository;
        this.auditRepository = auditRepository;
        this.eventProducer = eventProducer;
        this.deadLetterService = deadLetterService;
    }

    @Transactional
    public void ingest(OrderCreatedEvent event) {
        if (event.getChannels() == null || event.getChannels().isEmpty()) {
            log.warn("OrderCreatedEvent for order {} has no notification channels; nothing to do", event.getOrderId());
            return;
        }
        for (NotificationChannel channel : event.getChannels()) {
            ingestChannel(event, channel);
        }
    }

    private void ingestChannel(OrderCreatedEvent event, NotificationChannel channel) {
        String recipient = resolveRecipient(channel, event);

        Notification notification = new Notification(event.getOrderId(), channel, recipient == null ? "" : recipient, event.getCorrelationId());
        notification = notificationRepository.save(notification);
        auditRepository.save(new NotificationAudit(notification.getId(), "RECEIVED", "order-created consumed for order " + event.getOrderId()));

        if (recipient == null || recipient.isBlank()) {
            deadLetterService.deadLetter(notification,
                    "Missing " + channel + " contact details for user " + event.getUserId(), event.getCorrelationId());
            return;
        }

        eventProducer.publishRequested(new NotificationRequestedEvent(
                event.getCorrelationId(), notification.getId(), event.getOrderId(), event.getOrderNumber(),
                event.getTotal(), channel, recipient));
    }

    private String resolveRecipient(NotificationChannel channel, OrderCreatedEvent event) {
        return switch (channel) {
            case EMAIL -> event.getEmail();
            case SMS -> event.getPhone();
        };
    }
}
