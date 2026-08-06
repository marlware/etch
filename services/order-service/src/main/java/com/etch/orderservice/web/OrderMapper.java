package com.etch.orderservice.web;

import com.etch.dto.OrderResponse;
import com.etch.orderservice.domain.Order;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotal(),
                order.getChannels(),
                order.getCreatedAt()
        );
    }
}
