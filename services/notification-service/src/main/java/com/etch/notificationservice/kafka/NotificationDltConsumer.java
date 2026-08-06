package com.etch.notificationservice.kafka;

import com.etch.events.NotificationFailedEvent;
import com.etch.notificationservice.config.KafkaTopics;
import com.etch.notificationservice.domain.NotificationDlt;
import com.etch.notificationservice.domain.NotificationDltRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Archives everything published to {@code notification-dlt} into a table
 * so {@code GET /admin/dlt} has something to serve -- the topic itself
 * isn't queryable over REST. Consuming its own dead-letter topic (rather
 * than writing to the table directly from {@code DeadLetterService})
 * keeps the DLT topic as the single source of truth and lets other
 * consumers (e.g. an alerting pipeline) subscribe to it independently.
 */
@Component
public class NotificationDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDltConsumer.class);

    private final NotificationDltRepository dltRepository;
    private final ObjectMapper objectMapper;

    public NotificationDltConsumer(NotificationDltRepository dltRepository, ObjectMapper objectMapper) {
        this.dltRepository = dltRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_DLT,
            groupId = KafkaTopics.NOTIFICATION_DLT_CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onDeadLettered(NotificationFailedEvent event, Acknowledgment acknowledgment) {
        try {
            NotificationDlt record = new NotificationDlt(
                    event.getNotificationId(),
                    event.getOrderId(),
                    event.getChannel(),
                    toJson(event),
                    event.getReason(),
                    event.getRetryCount()
            );
            dltRepository.save(record);
            log.info("Archived DLT record for notification {} (order {})", event.getNotificationId(), event.getOrderId());
            acknowledgment.acknowledge();
        } catch (RuntimeException ex) {
            log.error("Failed to archive DLT record for notification {}", event.getNotificationId(), ex);
            throw ex;
        }
    }

    private String toJson(NotificationFailedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"failed to serialize original payload\"}";
        }
    }
}
