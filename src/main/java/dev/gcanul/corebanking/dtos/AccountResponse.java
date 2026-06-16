package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response object representing a banking account details")
public record AccountResponse(

        @Schema(description = "Internal database ID of the account", example = "10")
        Long id,

        @Schema(description = "Unique alphanumeric identifier for the account", example = "ACC-987654321")
        String accountNumber,

        @Schema(description = "Current available balance in the account", example = "1500.00")
        BigDecimal balance,

        @Schema(description = "Unique identifier of the user who owns the account", example = "1")
        Long userId
) {}