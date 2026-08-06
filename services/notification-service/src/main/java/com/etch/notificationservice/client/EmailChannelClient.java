package com.etch.notificationservice.client;

import com.etch.events.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmailChannelClient extends AbstractChannelClient {

    private static final Logger log = LoggerFactory.getLogger(EmailChannelClient.class);

    public EmailChannelClient(RestClient emailServiceRestClient) {
        super(emailServiceRestClient, "/email/send");
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    protected Logger logger() {
        return log;
    }
}
