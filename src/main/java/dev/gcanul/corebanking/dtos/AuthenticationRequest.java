package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload containing user credentials for login")
public record AuthenticationRequest(

        @Schema(description = "Registered username or email", example = "john.doe")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "User's password", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}