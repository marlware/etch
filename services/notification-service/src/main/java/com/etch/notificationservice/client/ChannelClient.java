package com.etch.notificationservice.client;

import com.etch.dto.SendMessageResponse;
import com.etch.events.NotificationChannel;

public interface ChannelClient {

    NotificationChannel channel();

    SendMessageResponse send(String recipient, String body, String correlationId);
}
