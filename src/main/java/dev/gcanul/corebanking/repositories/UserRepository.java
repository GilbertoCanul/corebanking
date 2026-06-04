package dev.gcanul.corebanking.repositories;

import dev.gcanul.corebanking.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA crea la implementación de esta query automáticamente
    Optional<User> findByUsername(String username);
}