package com.etch.common.web;

import com.etch.common.exception.DuplicateOrderException;
import com.etch.common.exception.KafkaException;
import com.etch.common.exception.NotificationException;
import com.etch.common.exception.ResourceNotFoundException;
import com.etch.common.exception.ValidationException;
import com.etch.dto.error.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractApiExceptionHandlerTest {

    private static class TestHandler extends AbstractApiExceptionHandler {
    }

    private final TestHandler handler = new TestHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");

    @Test
    void validationErrorsMapToBadRequestWithDetails() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidation(new ValidationException("bad input", List.of("total: must be positive")), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().details()).containsExactly("total: must be positive");
        assertThat(response.getBody().path()).isEqualTo("/orders");
    }

    @Test
    void missingResourcesMapToNotFound() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("No order found with id 9"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("No order found with id 9");
    }

    @Test
    void duplicateOrdersMapToConflict() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleDuplicateOrder(new DuplicateOrderException("already exists"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error()).isEqualTo("DUPLICATE_ORDER");
    }

    @Test
    void kafkaFailuresMapToServiceUnavailable() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleKafka(new KafkaException("broker down"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().error()).isEqualTo("KAFKA_ERROR");
    }

    @Test
    void notificationFailuresMapToBadGateway() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotification(new NotificationException("channel down", true), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().error()).isEqualTo("NOTIFICATION_ERROR");
    }

    @Test
    void unexpectedFailuresDoNotLeakTheirMessage() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("secret internals"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
