package com.etch.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ChannelClientConfig {

    @Value("${etch.channel-clients.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${etch.channel-clients.read-timeout-ms:3000}")
    private int readTimeoutMs;

    @Bean
    public RestClient emailServiceRestClient(@Value("${etch.channel-clients.email-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean
    public RestClient smsServiceRestClient(@Value("${etch.channel-clients.sms-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .build();
    }

    private ClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }
}
