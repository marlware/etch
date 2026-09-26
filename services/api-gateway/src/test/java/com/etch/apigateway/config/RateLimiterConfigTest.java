package com.etch.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;

class RateLimiterConfigTest {

    private final KeyResolver resolver = new RateLimiterConfig().clientAddressKeyResolver();

    @Test
    void keysByTheClientIpAddress() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders")
                        .remoteAddress(new InetSocketAddress(InetAddress.getByName("203.0.113.9"), 5000)));

        StepVerifier.create(resolver.resolve(exchange)).expectNext("203.0.113.9").verifyComplete();
    }

    @Test
    void fallsBackToUnknownWhenTheRemoteAddressIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

        StepVerifier.create(resolver.resolve(exchange)).expectNext("unknown").verifyComplete();
    }
}
