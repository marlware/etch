package com.etch.orderservice.dto;

import com.etch.dto.OrderRequest;
import com.etch.events.NotificationChannel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void acceptsAWellFormedRequest() {
        OrderRequest request = new OrderRequest(1L, "ORD-1001", new BigDecimal("19.99"), List.of(NotificationChannel.EMAIL));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsMissingUserId() {
        OrderRequest request = new OrderRequest(null, "ORD-1001", new BigDecimal("19.99"), List.of(NotificationChannel.EMAIL));
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    @Test
    void rejectsMalformedOrderNumber() {
        OrderRequest request = new OrderRequest(1L, "!!", new BigDecimal("19.99"), List.of(NotificationChannel.EMAIL));
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("orderNumber"));
    }

    @Test
    void rejectsNonPositiveTotal() {
        OrderRequest request = new OrderRequest(1L, "ORD-1001", BigDecimal.ZERO, List.of(NotificationChannel.EMAIL));
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("total"));
    }

    @Test
    void rejectsEmptyChannels() {
        OrderRequest request = new OrderRequest(1L, "ORD-1001", new BigDecimal("19.99"), List.of());
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("channels"));
    }
}
