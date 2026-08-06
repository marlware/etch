package com.etch.orderservice.web;

import com.etch.common.exception.DuplicateOrderException;
import com.etch.common.exception.ResourceNotFoundException;
import com.etch.common.logging.CorrelationIdConstants;
import com.etch.dto.OrderRequest;
import com.etch.dto.OrderResponse;
import com.etch.events.OrderCreatedEvent;
import com.etch.orderservice.domain.Order;
import com.etch.orderservice.domain.OrderRepository;
import com.etch.orderservice.domain.User;
import com.etch.orderservice.domain.UserRepository;
import com.etch.orderservice.kafka.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                         OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (orderRepository.existsByOrderNumber(request.orderNumber())) {
            throw new DuplicateOrderException("An order with number " + request.orderNumber() + " already exists");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id " + request.userId()));

        Order order = new Order(user.getId(), request.orderNumber(), request.total(), request.channels());
        order = orderRepository.save(order);
        order.markNotificationPending();
        order = orderRepository.save(order);

        log.info("Order {} created for user {} with total {}", order.getOrderNumber(), user.getId(), order.getTotal());

        String correlationId = MDC.get(CorrelationIdConstants.MDC_KEY);
        OrderCreatedEvent event = new OrderCreatedEvent(
                correlationId,
                order.getId(),
                order.getOrderNumber(),
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                order.getTotal(),
                order.getChannels()
        );
        orderEventProducer.publishOrderCreated(event);

        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(OrderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id " + id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderMapper::toResponse);
    }
}
