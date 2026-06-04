package dev.gcanul.corebanking.dtos;

import java.math.BigDecimal;

public record AccountRequest(
        String accountNumber,
        BigDecimal initialBalance,
        Long userId // ID del usuario dueño de la cuenta
) {}