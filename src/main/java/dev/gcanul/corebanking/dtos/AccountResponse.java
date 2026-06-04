package dev.gcanul.corebanking.dtos;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        Long userId
) {}