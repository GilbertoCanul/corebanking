package dev.gcanul.corebanking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Core Banking API",
                version = "1.0.0",
                description = "API documentation for the Core Banking System"
        )
)
@SecurityScheme(
        name = "bearerAuth", // This name is used as a reference in your controllers
        description = "JWT Authentication. Please enter your token in the format: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // This class sets up the global OpenAPI configuration and the JWT security scheme.
}