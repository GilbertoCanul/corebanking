package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload required to register a new user in the system")
public record RegisterRequest(

        @Schema(description = "Desired username for the new account", example = "john.doe")
        String username,

        @Schema(description = "Secure password for the new account", example = "SecurePass123!")
        String password
) {}