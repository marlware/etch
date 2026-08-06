package com.etch.events;

/**
 * Published once a notification has been successfully delivered by the
 * Email or SMS service.
 */
public class NotificationSentEvent extends BaseEvent {

    private Long notificationId;
    private Long orderId;
    private NotificationChannel channel;

    public NotificationSentEvent() {
        super();
    }

    public NotificationSentEvent(String correlationId, Long notificationId, Long orderId, NotificationChannel channel) {
        super(correlationId);
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.channel = channel;
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
}
