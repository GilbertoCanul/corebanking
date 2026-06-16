package dev.gcanul.corebanking.repositories;

import dev.gcanul.corebanking.entities.Account;
import dev.gcanul.corebanking.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTests {

    // Automatically manages the lifecycle of the container
    @Container
    @ServiceConnection // Automatically configures DB properties for Spring Boot
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void connectionEstablished() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldSaveAndFindAccount() {
        // 1. Arrange
        User user = User.builder()
                .username("testuser")
                .password("password123") // Cumpliendo con el not-null
                .build();
        // Asumiendo que tienes un UserRepository inyectado
        userRepository.save(user);

        // Ahora creamos la cuenta asociándola al usuario
        Account account = Account.builder()
                .accountNumber("ACC-12345")
                .balance(new BigDecimal("1000.00"))
                .user(user) // <--- ¡Aquí está la clave!
                .build();

        // 2. Act
        Account savedAccount = accountRepository.save(account);
        Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());

        // 3. Assert
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo("ACC-12345");
        assertThat(foundAccount.get().getBalance()).isEqualByComparingTo("1000.00");
    }
}