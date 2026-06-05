package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import dev.gcanul.corebanking.exceptions.AccountNotFoundException;
import dev.gcanul.corebanking.exceptions.InsufficientFundsException;
import dev.gcanul.corebanking.mappers.AccountMapper;
import dev.gcanul.corebanking.repositories.AccountRepository;
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
    void shouldThrowException_WhenUserDoesNotExist() {
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
    void shouldThrowException_WhenInitialBalanceIsNegative() {
        var request = new AccountRequest("1234567890", new BigDecimal("-100.00"), 99L);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Initial balance cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception when account number is null or empty")
    void shouldThrowException_WhenAccountNumberIsInvalid() {
        // 1. Arrange
        var requestWithNullAccountNumber = new AccountRequest(null, new BigDecimal("100.00"), 1L);
        var requestWithEmptyAccountNumber = new AccountRequest("", new BigDecimal("100.00"), 1L);

        // 2. Act & 3. Assert
        assertThatThrownBy(() -> accountService.createAccount(requestWithNullAccountNumber))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> accountService.createAccount(requestWithEmptyAccountNumber))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowException_WhenUserIdIsNull() {
        // 1. Arrange
        var request = new AccountRequest("1234567890", new BigDecimal("100.00"), null);

        // 2. Act & 3. Assert
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
    @DisplayName("Should throw AccountNotFoundException when depositing to non-existent account")
    void shouldThrowAccountNotFoundException_WhenDepositingToNonExistentAccount() {
        // 1. Arrange
        Long nonExistentId = 999L;
        BigDecimal amount = new BigDecimal("50.00");

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        assertThatThrownBy(() -> accountService.deposit(nonExistentId, amount))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with ID: 999");

        // Verify
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should successfully withdraw money from account")
    void shouldSuccessfullyWithdrawMoney() {
        // 1. Arrange
        Long accountId = 1L;
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal withdrawalAmount = new BigDecimal("50.00");

        Account account = Account.builder()
                .id(accountId)
                .balance(initialBalance)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // 2. Act
        accountService.withdraw(accountId, withdrawalAmount);

        // 3. Assert
        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
        assertThat(account.getTransactions()).hasSize(1);
        assertThat(account.getTransactions().getFirst().getAmount()).isEqualByComparingTo(withdrawalAmount);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when withdrawing from non-existent account")
    void shouldThrowAccountNotFoundException_WhenWithdrawingFromNonExistentAccount() {
        // 1. Arrange
        Long nonExistentId = 999L;
        BigDecimal amount = new BigDecimal("50.00");

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        assertThatThrownBy(() -> accountService.withdraw(nonExistentId, amount))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with ID: 999");

        // Verify
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when balance is lower than withdrawal amount")
    void shouldThrowInsufficientFundsException_WhenBalanceIsLowerThanAmount() {
        // 1. Arrange
        Long accountId = 1L;
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal withdrawalAmount = new BigDecimal("100.00");

        Account account = Account.builder()
                .id(accountId)
                .balance(initialBalance)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // 2. Act & 3. Assert
        assertThatThrownBy(() -> accountService.withdraw(accountId, withdrawalAmount))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for withdrawal");
        assertThat(account.getBalance()).isEqualByComparingTo(initialBalance);
        assertThat(account.getTransactions()).isEmpty();

        // Verify
        verify(accountRepository, times(1)).findById(accountId);
    }
}