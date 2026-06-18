package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import dev.gcanul.corebanking.events.AccountCreatedEvent;
import dev.gcanul.corebanking.exceptions.AccountNotFoundException;
import dev.gcanul.corebanking.exceptions.InsufficientFundsException;
import dev.gcanul.corebanking.exceptions.UserNotFoundException;
import dev.gcanul.corebanking.mappers.AccountMapper;
import dev.gcanul.corebanking.producers.AccountEventProducer;
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

    @Mock
    private AccountEventProducer eventProducer;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Should save an account successfully")
    void shouldCreateAccountSuccessfully() {
        // 1. Arrange
        var initialBalance = new BigDecimal("5000.00");
        var accountRequest = new AccountRequest(initialBalance, 1L);
        var fakeUser = new User();

        // Configuramos los mappers
        var accountToSave = Account.builder()
                .accountNumber("ACC-2026-0001")
                .balance(initialBalance)
                .user(fakeUser)
                .build();
        when(accountMapper.toEntity(any(AccountRequest.class), anyString(), eq(fakeUser)))
                .thenReturn(accountToSave);

        var expectedResponse = new AccountResponse(1L, "ACC-2026-0001", initialBalance, 1L);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(expectedResponse);

        // Configuramos el "stub" para el repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(accountToSave);

        // 2. Act
        AccountResponse accountResponse = accountService.createAccount(accountRequest);

        // 3. Assert
        assertThat(accountResponse).isEqualTo(expectedResponse);

        // Verificamos la interacción con el repository
        verify(accountRepository).save(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();

        // Verify interaction with mapper
        verify(accountMapper).toResponse(accountToSave);

        assertThat(capturedAccount.getAccountNumber()).isEqualTo("ACC-2026-0001"); // Verificamos formato
        assertThat(capturedAccount.getBalance()).isEqualByComparingTo(initialBalance);
        assertThat(capturedAccount.getUser()).isEqualTo(fakeUser);

        verify(eventProducer).sendAccountCreatedEvent(any(AccountCreatedEvent.class));
    }

    @Test
    @DisplayName("Should retry when account number already exists")
    void shouldRetryWhenAccountNumberAlreadyExists() {
        // 1. Arrange
        var initialBalance = new BigDecimal("5000.00");
        var accountRequest = new AccountRequest(initialBalance, 1L);
        var fakeUser = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        var accountToSave = Account.builder()
                .accountNumber("ACC-2026-0001")
                .balance(initialBalance)
                .user(fakeUser)
                .build();

        when(accountMapper.toEntity(any(), anyString(), any())).thenReturn(accountToSave);
        when(accountRepository.save(any(Account.class))).thenReturn(accountToSave);
        when(accountMapper.toResponse(any())).thenReturn(new AccountResponse(1L, "ACC-2026-0001", initialBalance, 1L));

        // 2. Act
        accountService.createAccount(accountRequest);

        // 3. Assert / Verify
        // Verificamos que el repositorio verificó la existencia DOS veces
        verify(accountRepository, times(2)).existsByAccountNumber(anyString());

        // Verificamos que al final sí guardó la cuenta
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw an exception when user does not exist")
    void shouldThrowException_WhenUserDoesNotExist() {
        // 1. Arrange
        var request = new AccountRequest(new BigDecimal("1000.00"), 99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 99");

        // 3. Verify side effects
        verify(accountRepository, never()).save(any(Account.class));
    }

//    @Test
//    @DisplayName("Should throw exception when initial balance is negative")
//    void shouldThrowException_WhenInitialBalanceIsNegative() {
//        var request = new AccountRequest(new BigDecimal("-100.00"), 99L);
//
//        assertThatThrownBy(() -> accountService.createAccount(request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("Initial balance cannot be negative");
//    }

//    @Test
//    @DisplayName("Should throw exception when user ID is null")
//    void shouldThrowException_WhenUserIdIsNull() {
//        // 1. Arrange
//        var request = new AccountRequest(new BigDecimal("100.00"), null);
//
//        // 2. Act & 3. Assert
//        assertThatThrownBy(() -> accountService.createAccount(request))
//                .isInstanceOf(UserNotFoundException.class)
//                .hasMessage("User ID cannot be null");
//    }

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

    @Test
    @DisplayName("Should transfer money successfully between two different accounts")
    void shouldTransferSuccessfully_WhenFundsAreSufficient() {
        // 1. Arrange
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        Account sender = Account.builder().id(fromId).balance(BigDecimal.valueOf(500)).build();
        Account receiver = Account.builder().id(toId).balance(BigDecimal.valueOf(100)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(receiver));

        // 2. Act
        accountService.transfer(fromId, toId, amount);

        // 3. Assert
        assertThat(sender.getBalance()).isEqualByComparingTo("400");
        assertThat(receiver.getBalance()).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("Should throw exception when funds are insufficient")
    void shouldThrowException_WhenInsufficientFunds() {
        // Arrange
        Long fromId = 1L;
        Long toId = 2L;
        Account sender = Account.builder().id(fromId).balance(BigDecimal.valueOf(50)).build();
        Account receiver = Account.builder().id(toId).balance(BigDecimal.valueOf(100)).build();

        when(accountRepository.findById(fromId)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(receiver));

        // Act & Assert
        assertThatThrownBy(() -> accountService.transfer(fromId, toId, BigDecimal.valueOf(100)))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for withdrawal");
    }

    @Test
    @DisplayName("Should throw exception when trying to transfer to same account")
    void shouldThrowException_WhenTransferingToSameAccount() {
        // Act & Assert
        assertThatThrownBy(() -> accountService.transfer(1L, 1L, BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer to the same account");
    }
}