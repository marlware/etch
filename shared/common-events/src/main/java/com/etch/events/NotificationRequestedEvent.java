package com.etch.events;

import java.math.BigDecimal;

/**
 * Published (and consumed) by Notification Service once a channel has been
 * selected for a given order and is ready to be dispatched to the
 * Email/SMS service. Separating "requested" from "order-created" lets the
 * dispatch worker be retried/scaled independently of ingestion. Carries
 * enough order detail (number, total) for the dispatcher to render a
 * templated message without a second lookup.
 */
public class NotificationRequestedEvent extends BaseEvent {

    private Long notificationId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal orderTotal;
    private NotificationChannel channel;
    private String recipient;

    public NotificationRequestedEvent() {
        super();
    }

    public NotificationRequestedEvent(String correlationId, Long notificationId, Long orderId, String orderNumber,
                                       BigDecimal orderTotal, NotificationChannel channel, String recipient) {
        super(correlationId);
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderTotal = orderTotal;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
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
