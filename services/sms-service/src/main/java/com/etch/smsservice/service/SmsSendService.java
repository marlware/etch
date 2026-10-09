package com.etch.smsservice.service;

import com.etch.dto.SendMessageRequest;
import com.etch.dto.SendMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for a real SMS provider (Twilio, SNS, ...). See
 * {@code EmailSendService} in email-service for why the behavior is
 * probability-driven rather than hardcoded.
 */
@Service
public class SmsSendService {

    private static final Logger log = LoggerFactory.getLogger(SmsSendService.class);

    private final double unavailableRate;
    private final double failureRate;

    public SmsSendService(@Value("${etch.simulation.unavailable-rate:0.0}") double unavailableRate,
                           @Value("${etch.simulation.failure-rate:0.05}") double failureRate) {
        if (unavailableRate < 0 || failureRate < 0 || unavailableRate + failureRate > 1.0) {
            throw new IllegalArgumentException(
                    "etch.simulation rates must be non-negative and sum to at most 1.0 (unavailable="
                            + unavailableRate + ", failure=" + failureRate + ")");
        }
        this.unavailableRate = unavailableRate;
        this.failureRate = failureRate;
    }

    public SendMessageResponse send(SendMessageRequest request) {
        double roll = ThreadLocalRandom.current().nextDouble();

        if (roll < unavailableRate) {
            log.warn("Simulating SMS provider outage for recipient {}", request.recipient());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS provider temporarily unavailable");
        }

        if (roll < unavailableRate + failureRate) {
            log.info("Simulated delivery failure for recipient {}", request.recipient());
            return SendMessageResponse.failure("Carrier rejected the message");
        }

        String providerMessageId = "sms-" + UUID.randomUUID();
        log.info("SMS sent to {} providerMessageId={}", request.recipient(), providerMessageId);
        return SendMessageResponse.success(providerMessageId);
    }
}
