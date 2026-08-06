package com.etch.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    /**
     * Rate-limits per client IP. The JWT filter runs before routing, so by
     * the time a request reaches the rate limiter it's already either
     * authenticated or headed for a public route -- keying by address
     * keeps this resolver independent of that filter's implementation.
     */
    @Bean
    public KeyResolver clientAddressKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String key = (remoteAddress != null && remoteAddress.getAddress() != null)
                    ? remoteAddress.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(key);
        };
    }
}
