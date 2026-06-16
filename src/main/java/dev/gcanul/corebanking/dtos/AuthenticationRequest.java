package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload containing user credentials for login")
public record AuthenticationRequest(

        @Schema(description = "Registered username or email", example = "john.doe")
        String username,

        @Schema(description = "User's password", example = "SecurePass123!")
        String password
) {}