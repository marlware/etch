package com.etch.events;

/**
 * Published when a notification permanently fails, either after exhausting
 * retries or immediately for a non-retryable error. Mirrors the payload
 * written to the {@code notification-dlt} dead letter topic.
 */
public class NotificationFailedEvent extends BaseEvent {

    private Long notificationId;
    private Long orderId;
    private NotificationChannel channel;
    private String reason;
    private int retryCount;

    public NotificationFailedEvent() {
        super();
    }

    public NotificationFailedEvent(String correlationId, Long notificationId, Long orderId,
                                    NotificationChannel channel, String reason, int retryCount) {
        super(correlationId);
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.channel = channel;
        this.reason = reason;
        this.retryCount = retryCount;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
