package com.etch.notificationservice.config;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order-created";
    public static final String NOTIFICATION_REQUESTED = "notification-requested";
    public static final String NOTIFICATION_SENT = "notification-sent";
    public static final String NOTIFICATION_FAILED = "notification-failed";
    public static final String NOTIFICATION_DLT = "notification-dlt";

    public static final String ORDER_CREATED_CONSUMER_GROUP = "notification-service-order-created";
    public static final String NOTIFICATION_REQUESTED_CONSUMER_GROUP = "notification-service-dispatch";
    public static final String NOTIFICATION_DLT_CONSUMER_GROUP = "notification-service-dlt";

    private KafkaTopics() {
    }
}
