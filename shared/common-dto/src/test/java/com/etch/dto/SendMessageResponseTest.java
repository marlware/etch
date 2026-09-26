package com.etch.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendMessageResponseTest {

    @Test
    void successCarriesProviderMessageIdAndNoFailureReason() {
        SendMessageResponse response = SendMessageResponse.success("email-123");

        assertThat(response.success()).isTrue();
        assertThat(response.providerMessageId()).isEqualTo("email-123");
        assertThat(response.failureReason()).isNull();
    }

    @Test
    void failureCarriesReasonAndNoProviderMessageId() {
        SendMessageResponse response = SendMessageResponse.failure("Mailbox full");

        assertThat(response.success()).isFalse();
        assertThat(response.providerMessageId()).isNull();
        assertThat(response.failureReason()).isEqualTo("Mailbox full");
    }
}
