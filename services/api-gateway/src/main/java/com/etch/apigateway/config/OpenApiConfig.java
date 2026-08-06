package com.etch.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGatewayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etch API Gateway")
                .description("Edge router: authentication, rate limiting, and request forwarding to downstream services")
                .version("v1"));
    }
}
