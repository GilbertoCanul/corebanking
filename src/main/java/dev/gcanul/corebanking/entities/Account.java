package dev.gcanul.corebanking.entities;

import dev.gcanul.corebanking.exceptions.InsufficientFundsException;
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
@SQLDelete(sql = "UPDATE accounts SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

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

        Transaction depositTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .build();

        this.transactions.add(depositTransaction);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("The withdrawal amount must be greater than zero");
        }

        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        this.balance = this.balance.subtract(amount);

        Transaction withdrawalTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .build();

        this.transactions.add(withdrawalTransaction);
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