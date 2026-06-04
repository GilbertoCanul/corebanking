package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.Transaction;
import dev.gcanul.corebanking.entities.User;
import dev.gcanul.corebanking.exceptions.AccountNotFoundException;
import dev.gcanul.corebanking.mappers.AccountMapper;
import dev.gcanul.corebanking.repositories.AccountRepository;
import dev.gcanul.corebanking.repositories.TransactionRepository;
import dev.gcanul.corebanking.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito
class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Should save an account successfully")
    void shouldCreateAccountSuccessfully() {
        // 1. Arrange
        var accountNumber = "1234567890";
        var initialBalance = new BigDecimal("5000.00");
        var accountRequest = new AccountRequest(accountNumber, initialBalance, 1L);
        var fakeUser = new User();

        var expectedResponse = new AccountResponse(1L, accountNumber, initialBalance, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(accountRepository.save(any(Account.class))).thenReturn(new Account());
        when(accountMapper.toResponse(any(Account.class))).thenReturn(expectedResponse);

        // 2. Act
        AccountResponse accountResponse = accountService.createAccount(accountRequest);

        // 3. Assert

        // Validate the contract (the DTO)
        assertThat(accountResponse)
                .as("The service response should match expected one")
                .isEqualTo(expectedResponse);

        // Validate the interaction (the repository)
        verify(accountRepository).save(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();

        assertThat(capturedAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(capturedAccount.getBalance()).isEqualByComparingTo(initialBalance);
        assertThat(capturedAccount.getUser()).isEqualTo(fakeUser);
    }

    @Test
    @DisplayName("Should throw an exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // 1. Arrange
        var request = new AccountRequest("0987654321", new BigDecimal("1000.00"), 99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: 99");

        // 3. Verify side effects
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw exception when initial balance is negative")
    void shouldThrowExceptionWhenInitialBalanceIsNegative() {
        var request = new AccountRequest("1234567890", new BigDecimal("-100.00"), 99L);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Initial balance cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when account number is null or empty")
    void shouldThrowExceptionWhenAccountNumberIsInvalid() {
        // Escenario: cuenta nula
        var requestWithNullAccountNumber = new AccountRequest(null, new BigDecimal("100.00"), 1L);

        // Escenario: cuenta vacía
        var requestWithEmptyAccountNumber = new AccountRequest("", new BigDecimal("100.00"), 1L);

        assertThatThrownBy(() -> accountService.createAccount(requestWithNullAccountNumber))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> accountService.createAccount(requestWithEmptyAccountNumber))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        var request = new AccountRequest("1234567890", new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID cannot be null");
    }

    @Test
    @DisplayName("Should successfully deposit money into account")
    void shouldSuccessfullyDepositMoney() {
        // 1. Arrange
        Long accountId = 1L;
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal depositAmount = new BigDecimal("50.00");

        Account account = Account.builder()
                .id(accountId)
                .balance(initialBalance)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // 2. Act
        accountService.deposit(accountId, depositAmount);

        // 3. Assert
        assertThat(account.getBalance()).isEqualByComparingTo("150.00");
        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getTransactions().getFirst().getAmount()).isEqualByComparingTo(depositAmount);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void shouldThrowExceptionWhenAccountNotFound() {
        // 1. Arrange
        Long nonExistentId = 999L;
        BigDecimal amount = new BigDecimal("50.00");

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThatThrownBy(() -> accountService.deposit(nonExistentId, amount))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with ID: 999");

        // 3. Verify
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay fondos suficientes para el retiro")
    void shouldThrowExceptionWhenWithdrawalHasInsufficientFunds() {
        // 1. Arrange
        Long accountId = 1L;
        BigDecimal withdrawalAmount = new BigDecimal("2000.00"); // Quiere sacar $2000

        var mockAccount = Account.builder()
                .id(accountId)
                .balance(new BigDecimal("1000.00")) // Pero solo tiene $1000
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // 2. Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> accountService.withdraw(accountId, withdrawalAmount));

        assertEquals("Insufficient funds for withdrawal.", exception.getMessage());

        // 3. Verify: Nos aseguramos de que NADIE guardó NADA
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}