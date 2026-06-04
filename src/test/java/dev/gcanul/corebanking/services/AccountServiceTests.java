package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.Transaction;
import dev.gcanul.corebanking.entities.TransactionType;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito
class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository; // El "doble" o simulacro del

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor; // ¡El cazador de objetos!

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @InjectMocks
    private AccountService accountService; // La clase que estamos probando

    @Test
    @DisplayName("Debe guardar una cuenta correctamente")
    void shouldCreateAccountSuccessfully() {
        // 1. Arrange (Preparar el escenario)
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

        // 3. Assert (Aquí ocurre la magia de AssertJ)

        // Validar el Contrato (El DTO)
        assertThat(accountResponse)
                .as("La respuesta del servicio debe coincidir con la esperada") // Mensaje personalizado si falla
                .isEqualTo(expectedResponse);

        // Validar la Interacción (El Repositorio)
        verify(accountRepository).save(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();

        assertThat(capturedAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(capturedAccount.getBalance()).isEqualByComparingTo(initialBalance);
        assertThat(capturedAccount.getUser()).isEqualTo(fakeUser);
    }

    @Test
    @DisplayName("Debe lanzar una excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // 1. Arrange
        // Mandamos un userId que sabemos que no existe (ej. 99L)
        var invalidRequest = new AccountRequest("0987654321", new BigDecimal("1000.00"), 99L);

        // Le enseñamos al mock del UserRepository: "Si te piden el ID 99, devuelve un Optional vacío"
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & Assert
        // Validamos que ejecutar el método lance la excepción correcta
        assertThrows(RuntimeException.class, () -> { // Cambia RuntimeException por tu excepción personalizada si tienes una
            accountService.createAccount(invalidRequest);
        });

        // 3. Verify
        // ¡LA PARTE MÁS IMPORTANTE!
        // Verificamos que, debido al error, el repositorio de cuentas NUNCA llamó al método save()
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Debe depositar dinero exitosamente y guardar la transacción")
    void shouldDepositSuccessfully() {
        // 1. Arrange
        Long accountId = 1L;
        BigDecimal depositAmount = new BigDecimal("500.00");

        var mockAccount = Account.builder()
                .id(accountId)
                .balance(new BigDecimal("1000.00")) // Saldo inicial: $1000
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // 2. Act
        accountService.deposit(accountId, depositAmount);

        // 3. Assert
        // Verificamos que el saldo en el objeto Account cambió a $1500
        assertEquals(new BigDecimal("1500.00"), mockAccount.getBalance());

        // Verificamos que se llamó al guardado de la cuenta
        verify(accountRepository, times(1)).save(mockAccount);

        // ¡LA MAGIA DEL CAPTOR! Atrapamos la transacción que se intentó guardar
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();

        // Validamos el contenido de la transacción atrapada
        assertEquals(TransactionType.DEPOSIT, savedTransaction.getType());
        assertEquals(depositAmount, savedTransaction.getAmount());
        assertEquals(mockAccount, savedTransaction.getAccount());
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
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            accountService.withdraw(accountId, withdrawalAmount);
        });

        assertEquals("Insufficient funds for withdrawal.", exception.getMessage());

        // 3. Verify: Nos aseguramos de que NADIE guardó NADA
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}