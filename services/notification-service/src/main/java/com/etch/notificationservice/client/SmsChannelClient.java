package com.etch.notificationservice.client;

import com.etch.events.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SmsChannelClient extends AbstractChannelClient {

    private static final Logger log = LoggerFactory.getLogger(SmsChannelClient.class);

    public SmsChannelClient(RestClient smsServiceRestClient) {
        super(smsServiceRestClient, "/sms/send");
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    protected Logger logger() {
        return log;
    }
}
