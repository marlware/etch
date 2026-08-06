package com.etch.notificationservice.kafka;

import com.etch.events.NotificationFailedEvent;
import com.etch.events.NotificationRequestedEvent;
import com.etch.events.NotificationSentEvent;
import com.etch.notificationservice.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRequested(NotificationRequestedEvent event) {
        send(KafkaTopics.NOTIFICATION_REQUESTED, event.getNotificationId().toString(), event);
    }

    public void publishSent(NotificationSentEvent event) {
        send(KafkaTopics.NOTIFICATION_SENT, event.getNotificationId().toString(), event);
    }

    public void publishFailed(NotificationFailedEvent event) {
        send(KafkaTopics.NOTIFICATION_FAILED, event.getNotificationId().toString(), event);
    }

    public void publishToDlt(NotificationFailedEvent event) {
        send(KafkaTopics.NOTIFICATION_DLT, event.getNotificationId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish to topic {} key={}", topic, key, ex);
            } else {
                log.debug("Published to topic {} key={} offset={}", topic, key, result.getRecordMetadata().offset());
            }
        });
    }
}
