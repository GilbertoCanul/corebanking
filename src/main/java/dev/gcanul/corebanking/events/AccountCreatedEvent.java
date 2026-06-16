package dev.gcanul.corebanking.events;

import java.math.BigDecimal;

public record AccountCreatedEvent(
        Long accountId,
        String accountNumber,
        BigDecimal initialBalance,
        Long userId
) {}