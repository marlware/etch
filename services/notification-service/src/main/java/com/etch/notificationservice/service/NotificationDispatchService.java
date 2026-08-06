package com.etch.notificationservice.service;

import com.etch.common.exception.NotificationException;
import com.etch.dto.SendMessageResponse;
import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import com.etch.notificationservice.client.ChannelClient;
import com.etch.notificationservice.domain.Notification;
import com.etch.notificationservice.domain.NotificationRepository;
import com.etch.notificationservice.domain.NotificationStatus;
import com.etch.notificationservice.metrics.NotificationMetrics;
import com.etch.notificationservice.retry.BackOff;
import com.etch.notificationservice.template.NotificationMessageRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dispatches a requested notification to its channel service, retrying
 * transient failures with backoff (README: "Retry 1, Retry 2, Retry 3,
 * Dead Letter Queue") and dead-lettering immediately on non-retryable
 * ones (invalid payload, channel rejection). Retries happen synchronously
 * within a single Kafka listener invocation rather than by re-consuming
 * the topic, so the message is acknowledged exactly once, after the
 * outcome (sent or dead-lettered) is final.
 */
@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final Map<NotificationChannel, ChannelClient> clientsByChannel;
    private final NotificationRepository notificationRepository;
    private final DeadLetterService deadLetterService;
    private final DispatchOutcomeRecorder outcomeRecorder;
    private final NotificationMetrics metrics;
    private final NotificationMessageRenderer messageRenderer;
    private final BackOff backOff;
    private final int maxAttempts;

    public NotificationDispatchService(List<ChannelClient> channelClients,
                                        NotificationRepository notificationRepository,
                                        DeadLetterService deadLetterService,
                                        DispatchOutcomeRecorder outcomeRecorder,
                                        NotificationMetrics metrics,
                                        NotificationMessageRenderer messageRenderer,
                                        BackOff backOff,
                                        @Value("${etch.retry.max-attempts:3}") int maxAttempts) {
        this.clientsByChannel = channelClients.stream()
                .collect(Collectors.toMap(ChannelClient::channel, Function.identity()));
        this.notificationRepository = notificationRepository;
        this.deadLetterService = deadLetterService;
        this.outcomeRecorder = outcomeRecorder;
        this.metrics = metrics;
        this.messageRenderer = messageRenderer;
        this.backOff = backOff;
        this.maxAttempts = maxAttempts;
    }

    public void dispatch(NotificationRequestedEvent requested) {
        Notification notification = notificationRepository.findById(requested.getNotificationId()).orElse(null);
        if (notification == null) {
            log.warn("Ignoring NotificationRequestedEvent for unknown notification {}", requested.getNotificationId());
            return;
        }
        if (notification.getStatus() != NotificationStatus.PENDING && notification.getStatus() != NotificationStatus.FAILED) {
            log.info("Notification {} already in terminal state {}, skipping duplicate dispatch",
                    notification.getId(), notification.getStatus());
            return;
        }

        ChannelClient client = clientsByChannel.get(requested.getChannel());
        String subject = messageRenderer.renderSubject(requested.getChannel(), requested);
        String body = messageRenderer.renderBody(requested.getChannel(), requested);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                SendMessageResponse response = client.send(notification.getRecipient(), subject, body, requested.getCorrelationId());
                outcomeRecorder.recordSuccess(notification, requested, response);
                return;
            } catch (NotificationException ex) {
                outcomeRecorder.recordFailedAttempt(notification, attempt, maxAttempts, ex);
                boolean lastAttempt = attempt == maxAttempts;
                if (!ex.isRetryable() || lastAttempt) {
                    deadLetterService.deadLetter(notification, ex.getMessage(), requested.getCorrelationId());
                    return;
                }
                metrics.incrementRetried();
                backOff.waitBeforeRetry(attempt);
            }
        }
    }
}
