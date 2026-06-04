package dev.gcanul.corebanking.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
// 1. Sobrescribimos el DELETE para que sea un UPDATE
@SQLDelete(sql = "UPDATE accounts SET deleted = true WHERE id = ?")
// 2. Filtramos automáticamente para que las consultas ignoren los eliminados
@Where(clause = "deleted = false")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id", nullable = false)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        this.balance = this.balance.add(amount);

        // B) Registramos la evidencia de forma atómica
        // Usamos el builder recordando que el timestamp se maneja con @PrePersist
        Transaction depositTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .build();

        // Al agregarla a la lista, CascadeType.ALL se encargará de guardarla en la DB
        this.transactions.add(depositTransaction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", accountNumber='" + accountNumber + "'}";
    }
}