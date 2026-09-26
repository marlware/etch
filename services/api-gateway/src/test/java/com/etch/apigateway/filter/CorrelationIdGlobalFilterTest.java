package com.etch.apigateway.filter;

import com.etch.common.logging.CorrelationIdConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdGlobalFilterTest {

    private final CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        chain = Mockito.mock(GatewayFilterChain.class);
        when(chain.filter(Mockito.any())).thenReturn(Mono.empty());
    }

    private ServerWebExchange forwardedExchange() {
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        return captor.getValue();
    }

    @Test
    void keepsTheCallersCorrelationIdOnBothRequestAndResponse() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders").header(CorrelationIdConstants.HEADER_NAME, "caller-id"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(forwardedExchange().getRequest().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo("caller-id");
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo("caller-id");
    }

    @Test
    void generatesAnIdWhenTheCallerDidNotSendOne() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String forwarded = forwardedExchange().getRequest().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);
        assertThat(forwarded).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo(forwarded);
    }

    @Test
    void runsBeforeEveryOtherFilter() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }
}
