package com.etch.emailservice.service;

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
 * Stands in for a real email provider (SES, SendGrid, ...). Behavior is
 * driven by configurable probabilities so the rest of the pipeline
 * (retry, DLQ) can be exercised deterministically in tests and demos:
 * {@code etch.simulation.unavailable-rate} raises a 503 (retryable from
 * the caller's point of view), {@code etch.simulation.failure-rate}
 * returns a normal-looking rejection (non-retryable).
 */
@Service
public class EmailSendService {

    private static final Logger log = LoggerFactory.getLogger(EmailSendService.class);

    private final double unavailableRate;
    private final double failureRate;

    public EmailSendService(@Value("${etch.simulation.unavailable-rate:0.0}") double unavailableRate,
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
            log.warn("Simulating email provider outage for recipient {}", request.recipient());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email provider temporarily unavailable");
        }

        if (roll < unavailableRate + failureRate) {
            log.info("Simulated delivery failure for recipient {}", request.recipient());
            return SendMessageResponse.failure("Recipient mailbox rejected the message");
        }

        String providerMessageId = "email-" + UUID.randomUUID();
        log.info("Email sent to {} subject='{}' providerMessageId={}", request.recipient(), request.subject(), providerMessageId);
        return SendMessageResponse.success(providerMessageId);
    }
}
