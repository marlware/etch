package com.etch.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_REQUESTED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationSentTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_SENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationFailedTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_DLT).partitions(1).replicas(1).build();
    }
}
