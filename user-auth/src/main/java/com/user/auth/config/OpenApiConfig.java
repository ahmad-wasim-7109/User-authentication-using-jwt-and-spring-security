package com.user.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Authentication API")
                        .version("1.0")
                        .description("API for user registration, login, and OTP verification"))
                .components(new Components()
                        .addSecuritySchemes("AuthorizationHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("AuthorizationHeader"))
                .tags(Arrays.asList(
                new Tag().name("1. Authentication Controller").description("Handles user authentication"),
                new Tag().name("2. Split Controller").description("Handles splitting logic"),
                new Tag().name("3. Group Controller").description("Manages group operations for split")
        ));
    }
}