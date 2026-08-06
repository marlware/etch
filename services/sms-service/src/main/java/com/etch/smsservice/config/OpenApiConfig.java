package com.etch.smsservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smsServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etch SMS Service")
                .description("Simulates SMS delivery and reports success/failure back to the caller")
                .version("v1"));
    }
}
