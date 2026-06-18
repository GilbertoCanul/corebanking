package dev.gcanul.corebanking.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Payload required to create a new banking account")
public record AccountRequest(

        @Schema(description = "Initial deposit amount to open the account", example = "1500.00")
        @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
        BigDecimal initialBalance,

        @Schema(description = "Unique identifier of the user who owns the account", example = "1")
        @NotNull(message = "User ID cannot be null")
        Long userId
) {}