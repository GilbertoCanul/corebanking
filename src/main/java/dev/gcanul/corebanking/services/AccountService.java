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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest accountRequest) {
        // 1. Validar que el usuario exista (NUEVA LÓGICA)
        User user = userRepository.findById(accountRequest.userId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + accountRequest.userId()));
        // Nota: Más adelante puedes cambiar RuntimeException por un UserNotFoundException personalizado

        // 2. Mapeo: De DTO (Record) a Entidad
        var account = new Account();
        account.setAccountNumber(accountRequest.accountNumber());
        account.setBalance(accountRequest.initialBalance());
        account.setUser(user); // <-- ¡VINCULAMOS LA CUENTA AL USUARIO!

        // 3. Guardar en la base de datos
        Account savedAccount = accountRepository.save(account);

        // 4. Mapeo: De Entidad a DTO (Record)
        return accountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        // Obtenemos la lista de entidades y usamos Streams de Java para transformarlas a DTOs
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toResponse)
                .toList(); // .toList() es nativo a partir de Java 16+
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        // 1. Domain Validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        // 2. Fetch Entity (or throw exception if not found)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        // 3. Apply business logic
        account.setBalance(account.getBalance().add(amount));

        // 4. Save (JPA will handle the update because of @Transactional)
        accountRepository.save(account);

        // Registro de auditoría
        createTransaction(account, amount, TransactionType.DEPOSIT);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        // 1. Domain Validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        // 2. Fetch Entity
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        // 3. Business Rule: Check for sufficient funds
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for withdrawal.");
        }

        // 4. Apply logic
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Registro de auditoría
        createTransaction(account, amount, TransactionType.WITHDRAWAL);
    }

    // Método privado para mantener el código DRY y centralizar la lógica de auditoría
    private void createTransaction(Account account, BigDecimal amount, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .type(type)
                .account(account) // ¡Aquí vinculamos la transacción con la cuenta!
                .build();

        transactionRepository.save(transaction);
    }
}