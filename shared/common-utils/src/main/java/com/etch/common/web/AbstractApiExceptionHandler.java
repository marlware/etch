package com.etch.common.web;

import com.etch.common.exception.DuplicateOrderException;
import com.etch.common.exception.KafkaException;
import com.etch.common.exception.NotificationException;
import com.etch.common.exception.ResourceNotFoundException;
import com.etch.common.exception.ValidationException;
import com.etch.common.logging.CorrelationIdConstants;
import com.etch.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * Base for each service's {@code @RestControllerAdvice}. Spring resolves
 * {@code @ExceptionHandler} methods declared on superclasses just as it
 * does on the advice bean itself, so services get a consistent error
 * envelope (see {@link ApiErrorResponse}) without duplicating this logic.
 */
public abstract class AbstractApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractApiExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request, ex.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Request payload failed validation: {}", details);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request payload failed validation", request, details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateOrder(DuplicateOrderException ex, HttpServletRequest request) {
        log.warn("Duplicate order rejected: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "DUPLICATE_ORDER", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ApiErrorResponse> handleKafka(KafkaException ex, HttpServletRequest request) {
        log.error("Kafka operation failed", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "KAFKA_ERROR", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiErrorResponse> handleNotification(NotificationException ex, HttpServletRequest request) {
        log.error("Notification dispatch failed", ex);
        return build(HttpStatus.BAD_GATEWAY, "NOTIFICATION_ERROR", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error, String message,
                                                     HttpServletRequest request, List<String> details) {
        String correlationId = MDC.get(CorrelationIdConstants.MDC_KEY);
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(), error, message, request.getRequestURI(), correlationId, details);
        return ResponseEntity.status(status).body(body);
    }
}
