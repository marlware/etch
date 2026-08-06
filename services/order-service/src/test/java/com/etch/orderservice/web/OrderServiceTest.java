package com.etch.orderservice.web;

import com.etch.common.exception.DuplicateOrderException;
import com.etch.common.exception.ResourceNotFoundException;
import com.etch.dto.OrderRequest;
import com.etch.dto.OrderResponse;
import com.etch.events.NotificationChannel;
import com.etch.events.OrderCreatedEvent;
import com.etch.orderservice.domain.Order;
import com.etch.orderservice.domain.OrderRepository;
import com.etch.orderservice.domain.User;
import com.etch.orderservice.domain.UserRepository;
import com.etch.orderservice.kafka.OrderEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userRepository, orderEventProducer);
    }

    @Test
    void createOrder_persistsOrderAndPublishesEvent() {
        OrderRequest request = new OrderRequest(1L, "ORD-1001", new BigDecimal("49.99"), List.of(NotificationChannel.EMAIL));
        User user = new User("ada@example.com", "+15551234567");
        when(orderRepository.existsByOrderNumber("ORD-1001")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.orderNumber()).isEqualTo("ORD-1001");
        assertThat(response.total()).isEqualByComparingTo("49.99");

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventProducer).publishOrderCreated(eventCaptor.capture());
        OrderCreatedEvent published = eventCaptor.getValue();
        assertThat(published.getOrderNumber()).isEqualTo("ORD-1001");
        assertThat(published.getEmail()).isEqualTo("ada@example.com");
        assertThat(published.getChannels()).containsExactly(NotificationChannel.EMAIL);
    }

    @Test
    void createOrder_rejectsDuplicateOrderNumber() {
        OrderRequest request = new OrderRequest(1L, "ORD-1001", new BigDecimal("10.00"), List.of(NotificationChannel.SMS));
        when(orderRepository.existsByOrderNumber("ORD-1001")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(DuplicateOrderException.class);

        verifyNoInteractions(orderEventProducer);
    }

    @Test
    void createOrder_rejectsUnknownUser() {
        OrderRequest request = new OrderRequest(99L, "ORD-1002", new BigDecimal("10.00"), List.of(NotificationChannel.SMS));
        when(orderRepository.existsByOrderNumber("ORD-1002")).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(orderEventProducer);
    }

    @Test
    void getOrder_throwsWhenMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
