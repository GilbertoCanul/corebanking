package dev.gcanul.corebanking.repositories;

import dev.gcanul.corebanking.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Aquí podríamos añadir métodos para buscar transacciones por cuenta o por rango de fechas
}