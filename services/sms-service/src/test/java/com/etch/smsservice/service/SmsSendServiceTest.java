package com.etch.smsservice.service;

import com.etch.dto.SendMessageRequest;
import com.etch.dto.SendMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsSendServiceTest {

    private static final SendMessageRequest REQUEST =
            new SendMessageRequest("+15551234567", null, "Thanks for your order!", "corr-1");

    @Test
    void alwaysSucceedsWhenBothSimulatedRatesAreZero() {
        SmsSendService service = new SmsSendService(0.0, 0.0);

        SendMessageResponse response = service.send(REQUEST);

        assertThat(response.success()).isTrue();
        assertThat(response.providerMessageId()).startsWith("sms-");
    }

    @Test
    void alwaysReturnsAFailureResponseWhenFailureRateIsCertain() {
        SmsSendService service = new SmsSendService(0.0, 1.0);

        SendMessageResponse response = service.send(REQUEST);

        assertThat(response.success()).isFalse();
        assertThat(response.failureReason()).isNotBlank();
    }

    @Test
    void raisesServiceUnavailableWhenOutageIsCertain() {
        SmsSendService service = new SmsSendService(1.0, 0.0);

        assertThatThrownBy(() -> service.send(REQUEST))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("503");
    }
}
