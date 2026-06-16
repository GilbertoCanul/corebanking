package dev.gcanul.corebanking.mappers;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public Account toEntity(AccountRequest accountRequest, String accountNumber, User user) {
        return Account.builder()
                .accountNumber(accountNumber)
                .balance(accountRequest.initialBalance())
                .user(user)
                .build();
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getUser() != null ? account.getUser().getId() : null
        );
    }
}