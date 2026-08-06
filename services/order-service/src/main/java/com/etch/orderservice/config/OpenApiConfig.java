package com.etch.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etch Order Service")
                .description("Receives, validates, and persists business orders, then publishes order-created events to Kafka")
                .version("v1"));
    }
}
