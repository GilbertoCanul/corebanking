package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standardized error response object for the API")
public record ErrorResponse(

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Detailed error message explaining what went wrong", example = "Invalid input data. Please check the provided fields.")
        String message,

        @Schema(description = "Timestamp when the error occurred", example = "2026-06-15T17:44:38")
        LocalDateTime timestamp
) {}