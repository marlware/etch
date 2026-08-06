package com.etch.notificationservice.kafka;

import com.etch.common.logging.CorrelationIdConstants;
import com.etch.events.NotificationRequestedEvent;
import com.etch.notificationservice.config.KafkaTopics;
import com.etch.notificationservice.redis.IdempotencyService;
import com.etch.notificationservice.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NotificationRequestedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestedConsumer.class);
    private static final String DISPATCH_LOCK_PREFIX = "dispatching:notification:";

    private final NotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;

    public NotificationRequestedConsumer(NotificationDispatchService dispatchService, IdempotencyService idempotencyService) {
        this.dispatchService = dispatchService;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_REQUESTED,
            groupId = KafkaTopics.NOTIFICATION_REQUESTED_CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onNotificationRequested(NotificationRequestedEvent event, Acknowledgment acknowledgment) {
        MDC.put(CorrelationIdConstants.MDC_KEY, event.getCorrelationId());
        // short-lived lock: dispatch already retries internally and can take a
        // few seconds, so this only needs to survive one in-flight attempt,
        // not the lifetime of the notification like the order-created claim does
        String lockKey = DISPATCH_LOCK_PREFIX + event.getNotificationId();
        try {
            log.info("Received NotificationRequestedEvent notificationId={} channel={}", event.getNotificationId(), event.getChannel());

            if (!idempotencyService.claim(lockKey, Duration.ofSeconds(30))) {
                log.info("Notification {} is already being dispatched elsewhere, skipping duplicate delivery", event.getNotificationId());
                acknowledgment.acknowledge();
                return;
            }

            dispatchService.dispatch(event);
            acknowledgment.acknowledge();
        } catch (RuntimeException ex) {
            log.error("Failed to process NotificationRequestedEvent notificationId={}", event.getNotificationId(), ex);
            throw ex;
        } finally {
            idempotencyService.release(lockKey);
            MDC.remove(CorrelationIdConstants.MDC_KEY);
        }
    }
}
