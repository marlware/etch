package com.etch.apigateway.config;

import com.etch.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${etch.jwt.secret}") String secret,
            @Value("${etch.jwt.expiration-ms:3600000}") long expirationMs) {
        return new JwtTokenProvider(secret, expirationMs);
    }
}
