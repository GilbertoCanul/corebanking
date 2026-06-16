package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the JWT token after successful authentication")
public record AuthenticationResponse(

        @Schema(description = "Bearer token to be used in the Authorization header", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {}