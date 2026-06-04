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
        if (accountRequest.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        if (accountRequest.accountNumber() == null || accountRequest.accountNumber().isBlank()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }

        if (accountRequest.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Validate that user exists
        User user = userRepository.findById(accountRequest.userId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + accountRequest.userId()));
        // Nota: Más adelante puedes cambiar RuntimeException por un UserNotFoundException personalizado

        // Mapping: from DTO (Record) to Entity
        var account = new Account();
        account.setAccountNumber(accountRequest.accountNumber());
        account.setBalance(accountRequest.initialBalance());
        account.setUser(user);

        // Save in the database
        Account savedAccount = accountRepository.save(account);

        // Mapping: from Entity to DTO (Record)
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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        account.deposit(amount);
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