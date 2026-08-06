package com.etch.events;

/**
 * Published (and consumed) by Notification Service once a channel has been
 * selected for a given order and is ready to be dispatched to the
 * Email/SMS service. Separating "requested" from "order-created" lets the
 * dispatch worker be retried/scaled independently of ingestion.
 */
public class NotificationRequestedEvent extends BaseEvent {

    private Long notificationId;
    private Long orderId;
    private NotificationChannel channel;
    private String recipient;

    public NotificationRequestedEvent() {
        super();
    }

    public NotificationRequestedEvent(String correlationId, Long notificationId, Long orderId,
                                       NotificationChannel channel, String recipient) {
        super(correlationId);
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.channel = channel;
        this.recipient = recipient;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
