package com.etch.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etch Notification Service")
                .description("Consumes order-created events, dispatches notifications to Email/SMS, and manages retries and the dead letter queue")
                .version("v1"));
    }
}
