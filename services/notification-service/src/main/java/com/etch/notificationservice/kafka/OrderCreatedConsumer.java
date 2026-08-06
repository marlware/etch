package com.etch.notificationservice.kafka;

import com.etch.common.logging.CorrelationIdConstants;
import com.etch.events.OrderCreatedEvent;
import com.etch.notificationservice.config.KafkaTopics;
import com.etch.notificationservice.redis.IdempotencyService;
import com.etch.notificationservice.service.NotificationIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);
    private static final String IDEMPOTENCY_PREFIX = "processed:order-created:";

    private final NotificationIngestService ingestService;
    private final IdempotencyService idempotencyService;

    public OrderCreatedConsumer(NotificationIngestService ingestService, IdempotencyService idempotencyService) {
        this.ingestService = ingestService;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = KafkaTopics.ORDER_CREATED_CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        MDC.put(CorrelationIdConstants.MDC_KEY, event.getCorrelationId());
        String idempotencyKey = IDEMPOTENCY_PREFIX + event.getEventId();
        try {
            log.info("Received OrderCreatedEvent orderId={} channels={}", event.getOrderId(), event.getChannels());

            if (!idempotencyService.claim(idempotencyKey)) {
                log.info("OrderCreatedEvent {} already processed, skipping duplicate delivery", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            ingestService.ingest(event);
            acknowledgment.acknowledge();
        } catch (RuntimeException ex) {
            // let a redelivery try again from scratch instead of permanently
            // treating this event as handled
            idempotencyService.release(idempotencyKey);
            log.error("Failed to process OrderCreatedEvent orderId={}", event.getOrderId(), ex);
            throw ex;
        } finally {
            MDC.remove(CorrelationIdConstants.MDC_KEY);
        }
    }
}
