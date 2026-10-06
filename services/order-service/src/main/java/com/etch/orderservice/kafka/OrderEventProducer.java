package com.etch.orderservice.kafka;

import com.etch.events.OrderCreatedEvent;
import com.etch.orderservice.config.KafkaTopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = event.getOrderId().toString();
        log.info("Publishing OrderCreatedEvent orderId={} correlationId={}", event.getOrderId(), event.getCorrelationId());
        kafkaTemplate.send(KafkaTopicConfig.ORDER_CREATED_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Fire-and-forget by design (README: "return immediately without
                        // waiting for notifications") -- log loudly since nothing else
                        // observes this failure; a stuck producer shows up in the logs
                        // via the eventual absence of a NotificationRequestedEvent instead.
                        log.error("Failed to publish OrderCreatedEvent orderId={}", event.getOrderId(), ex);
                        return;
                    }
                    log.debug("Published OrderCreatedEvent orderId={} partition={} offset={}",
                            event.getOrderId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
