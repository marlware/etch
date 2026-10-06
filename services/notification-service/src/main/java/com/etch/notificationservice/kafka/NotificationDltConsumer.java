package com.etch.notificationservice.kafka;

import com.etch.events.NotificationFailedEvent;
import com.etch.notificationservice.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Drains the {@code notification-dlt} dead-letter topic and logs each
 * record so permanently failed notifications are visible in the service
 * logs.
 */
@Component
public class NotificationDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDltConsumer.class);

    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_DLT,
            groupId = KafkaTopics.NOTIFICATION_DLT_CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onDeadLettered(NotificationFailedEvent event, Acknowledgment acknowledgment) {
        log.warn("Dead-lettered notification {} (order {}, channel {}) after {} attempts: {}",
                event.getNotificationId(), event.getOrderId(), event.getChannel(),
                event.getRetryCount(), event.getReason());
        acknowledgment.acknowledge();
    }
}
