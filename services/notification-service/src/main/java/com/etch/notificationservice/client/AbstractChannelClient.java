package com.etch.notificationservice.client;

import com.etch.common.exception.NotificationException;
import com.etch.dto.SendMessageRequest;
import com.etch.dto.SendMessageResponse;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

abstract class AbstractChannelClient implements ChannelClient {

    private final RestClient restClient;
    private final String sendPath;

    protected AbstractChannelClient(RestClient restClient, String sendPath) {
        this.restClient = restClient;
        this.sendPath = sendPath;
    }

    @Override
    public SendMessageResponse send(String recipient, String subject, String body, String correlationId) {
        SendMessageRequest request = new SendMessageRequest(recipient, subject, body, correlationId);
        try {
            SendMessageResponse response = restClient.post()
                    .uri(sendPath)
                    .body(request)
                    .retrieve()
                    .body(SendMessageResponse.class);

            if (response == null) {
                throw new NotificationException(channel() + " service returned an empty response", true);
            }
            if (!response.success()) {
                // the downstream channel service processed the request and made an
                // explicit delivery decision -- treat its failure as non-retryable,
                // retrying won't change a channel-level rejection.
                throw new NotificationException(
                        channel() + " delivery rejected: " + response.failureReason(), false);
            }
            return response;
        } catch (ResourceAccessException ex) {
            // connection refused / timeout -- the channel service is unreachable
            logger().warn("{} service unreachable for recipient {}: {}", channel(), recipient, ex.getMessage());
            throw new NotificationException(channel() + " service is unavailable", true, ex);
        } catch (HttpServerErrorException ex) {
            logger().warn("{} service returned {} for recipient {}", channel(), ex.getStatusCode(), recipient);
            throw new NotificationException(channel() + " service returned " + ex.getStatusCode(), true, ex);
        } catch (HttpClientErrorException ex) {
            logger().warn("{} service rejected request for recipient {}: {}", channel(), recipient, ex.getStatusCode());
            throw new NotificationException(channel() + " service rejected the request: " + ex.getStatusCode(), false, ex);
        }
    }

    protected abstract Logger logger();
}
