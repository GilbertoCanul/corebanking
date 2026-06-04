package dev.gcanul.corebanking.mappers;

import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getUser() != null ? account.getUser().getId() : null
        );
    }
}