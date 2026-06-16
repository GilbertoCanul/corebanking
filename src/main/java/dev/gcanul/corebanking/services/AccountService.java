package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AccountRequest;
import dev.gcanul.corebanking.dtos.AccountResponse;
import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import dev.gcanul.corebanking.events.AccountCreatedEvent;
import dev.gcanul.corebanking.exceptions.AccountNotFoundException;
import dev.gcanul.corebanking.mappers.AccountMapper;
import dev.gcanul.corebanking.producers.AccountEventProducer;
import dev.gcanul.corebanking.repositories.AccountRepository;
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
    private final AccountMapper accountMapper;
    private final UserRepository userRepository;
    private final AccountEventProducer eventProducer;

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

        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getUser().getId()
        );

        eventProducer.sendAccountCreatedEvent(event);

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        account.withdraw(amount);
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // 2. Ordenar IDs para prevenir Deadlocks
        Long firstId = Math.min(fromAccountId, toAccountId);
        Long secondId = Math.max(fromAccountId, toAccountId);

        // 3. Obtener cuentas en orden (esto evita el deadlock)
        // Nota: Aunque el fetch es "en orden", esto es para la lógica de bloqueo.
        // Los objetos luego los asignamos según corresponda.
        Account firstAccount = accountRepository.findById(firstId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + firstId));
        Account secondAccount = accountRepository.findById(secondId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + secondId));

        Account sender = (firstId.equals(fromAccountId)) ? firstAccount : secondAccount;
        Account receiver = (firstId.equals(toAccountId)) ? firstAccount : secondAccount;

        // 4. Ejecutar lógica de negocio (reutilizamos tus métodos de Account)
        sender.withdraw(amount);
        receiver.deposit(amount);
    }
}