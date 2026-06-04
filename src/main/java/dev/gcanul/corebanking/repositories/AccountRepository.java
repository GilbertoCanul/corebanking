package dev.gcanul.corebanking.repositories;

import dev.gcanul.corebanking.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Spring Boot implementará automáticamente los métodos:
    // save(), findById(), findAll(), deleteById(), etc.

    // Podemos añadir consultas personalizadas si las necesitamos:
    Optional<Account> findByAccountNumber(String accountNumber);
}