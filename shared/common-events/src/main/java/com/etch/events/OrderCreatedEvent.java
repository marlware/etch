package com.etch.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Published by Order Service to the {@code order-created} topic once an
 * order has been persisted. Consumed by Notification Service to kick off
 * the delivery pipeline.
 */
public class OrderCreatedEvent extends BaseEvent {

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String email;
    private String phone;
    private BigDecimal total;
    private List<NotificationChannel> channels;

    public OrderCreatedEvent() {
        super();
    }

    public OrderCreatedEvent(String correlationId, Long orderId, String orderNumber, Long userId,
                              String email, String phone, BigDecimal total, List<NotificationChannel> channels) {
        super(correlationId);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.total = total;
        this.channels = channels;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<NotificationChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<NotificationChannel> channels) {
        this.channels = channels;
    }
}
