package com.etch.apigateway.filter;

import com.etch.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationGlobalFilterTest {

    private static final String SECRET = "test-only-etch-gateway-secret-needs-32-bytes-min";

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationGlobalFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 60_000);
        filter = new JwtAuthenticationGlobalFilter(jwtTokenProvider, new ObjectMapper());
        chain = Mockito.mock(GatewayFilterChain.class);
        when(chain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    @Test
    void allowsPublicRouteWithoutAToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/auth/token"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain, times(1)).filter(Mockito.any());
    }

    @Test
    void rejectsProtectedRouteWithoutAToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain, never()).filter(Mockito.any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejectsProtectedRouteWithAnInvalidToken() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders").header("Authorization", "Bearer not-a-real-token"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain, never()).filter(Mockito.any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void allowsProtectedRouteWithAValidToken() {
        String token = jwtTokenProvider.generateToken("demo-user", Map.of());
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders").header("Authorization", "Bearer " + token));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain, times(1)).filter(Mockito.any());
    }
}
