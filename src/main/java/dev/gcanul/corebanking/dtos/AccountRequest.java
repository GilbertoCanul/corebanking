package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Payload required to create a new banking account")
public record AccountRequest(

        @Schema(description = "Unique alphanumeric identifier for the account", example = "ACC-987654321")
        String accountNumber,

        @Schema(description = "Initial deposit amount to open the account", example = "1500.00")
        BigDecimal initialBalance,

        @Schema(description = "Unique identifier of the user who owns the account", example = "1")
        Long userId // ID del usuario dueño de la cuenta
) {}