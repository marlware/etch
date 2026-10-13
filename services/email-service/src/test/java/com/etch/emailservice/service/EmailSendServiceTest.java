package com.etch.emailservice.service;

import com.etch.dto.SendMessageRequest;
import com.etch.dto.SendMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailSendServiceTest {

    private static final SendMessageRequest REQUEST =
            new SendMessageRequest("buyer@example.com", "Your order", "Thanks for your order!", "corr-1");

    @Test
    void alwaysSucceedsWhenBothSimulatedRatesAreZero() {
        EmailSendService service = new EmailSendService(0.0, 0.0);

        SendMessageResponse response = service.send(REQUEST);

        assertThat(response.success()).isTrue();
        assertThat(response.providerMessageId()).startsWith("email-");
    }

    @Test
    void alwaysReturnsAFailureResponseWhenFailureRateIsCertain() {
        EmailSendService service = new EmailSendService(0.0, 1.0);

        SendMessageResponse response = service.send(REQUEST);

        assertThat(response.success()).isFalse();
        assertThat(response.failureReason()).isNotBlank();
    }

    @Test
    void raisesServiceUnavailableWhenOutageIsCertain() {
        EmailSendService service = new EmailSendService(1.0, 0.0);

        assertThatThrownBy(() -> service.send(REQUEST))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("503");
    }

    @Test
    void rejectsRatesThatSumPastOne() {
        assertThatThrownBy(() -> new EmailSendService(0.6, 0.6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeRates() {
        assertThatThrownBy(() -> new EmailSendService(-0.1, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
