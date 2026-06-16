package dev.gcanul.corebanking.mappers;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountMapperTests {
    private final AccountMapper mapper = new AccountMapper();

    @Test
    @DisplayName("Should map AccountRequest to Account entity correctly")
    void shouldMapRequestToEntity() {
        // 1. Arrange
        var initialBalance = new BigDecimal("1000.00");
        var accountRequest = new AccountRequest(initialBalance, 1L);
        var user = User.builder().id(1L).build();
        var accountNumber = "ACC-2026-0001";

        // 2. Act
        Account account = mapper.toEntity(accountRequest, accountNumber, user);

        // 3. Assert
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getBalance()).isEqualByComparingTo(initialBalance);
        assertThat(account.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should map Account entity to AccountResponse DTO")
    void shouldMapAccountToResponse() {
        // Arrange
        var user = User.builder().id(55L).build();
        var account = Account.builder()
                .id(10L)
                .accountNumber("ACC-2026-9999")
                .balance(new BigDecimal("500.00"))
                .user(user)
                .build();

        // Act
        AccountResponse accountResponse = mapper.toResponse(account);

        // Assert
        assertThat(accountResponse.id()).isEqualTo(10L);
        assertThat(accountResponse.accountNumber()).isEqualTo("ACC-2026-9999");
        assertThat(accountResponse.balance()).isEqualByComparingTo("500.00");
        assertThat(accountResponse.userId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("Should return null userId when account has no user")
    void shouldMapAccountWithNullUser() {
        // Arrange
        var account = Account.builder()
                .id(10L)
                .accountNumber("ACC-2026-0000")
                .balance(BigDecimal.ZERO)
                .user(null) // Caso de borde
                .build();

        // Act
        AccountResponse response = mapper.toResponse(account);

        // Assert
        assertThat(response.userId()).isNull();
    }
}
