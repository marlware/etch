package com.etch.orderservice.web;

import com.etch.dto.OrderResponse;
import com.etch.events.NotificationChannel;
import com.etch.orderservice.domain.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    void mapsEntityFieldsAndExposesStatusAsName() {
        Order order = new Order(7L, "ORD-2002", new BigDecimal("12.50"), List.of(NotificationChannel.SMS));
        order.markNotificationPending();

        OrderResponse response = OrderMapper.toResponse(order);

        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.orderNumber()).isEqualTo("ORD-2002");
        assertThat(response.status()).isEqualTo("NOTIFICATION_PENDING");
        assertThat(response.total()).isEqualByComparingTo("12.50");
        assertThat(response.channels()).containsExactly(NotificationChannel.SMS);
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    void newOrdersStartAsReceived() {
        Order order = new Order(1L, "ORD-3003", BigDecimal.TEN, List.of(NotificationChannel.EMAIL));

        assertThat(OrderMapper.toResponse(order).status()).isEqualTo("RECEIVED");
    }
}
