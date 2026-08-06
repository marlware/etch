package com.etch.orderservice.domain;

import com.etch.events.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Convert(converter = NotificationChannelListConverter.class)
    @Column(nullable = false, length = 100)
    private List<NotificationChannel> channels;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Order() {
    }

    public Order(Long userId, String orderNumber, BigDecimal total, List<NotificationChannel> channels) {
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.total = total;
        this.channels = channels;
    }

    public void markNotificationPending() {
        this.status = OrderStatus.NOTIFICATION_PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<NotificationChannel> getChannels() {
        return channels;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
