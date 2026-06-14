-- Crear tabla de usuarios
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

-- Crear tabla de cuentas
CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          version BIGINT DEFAULT 0,
                          account_number VARCHAR(255) NOT NULL UNIQUE,
                          balance NUMERIC(19, 2) NOT NULL,
                          user_id BIGINT NOT NULL,
                          deleted BOOLEAN DEFAULT FALSE,
                          CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Crear tabla de transacciones
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              amount NUMERIC(19, 2) NOT NULL,
                              type VARCHAR(50) NOT NULL,
                              timestamp TIMESTAMP NOT NULL,
                              account_id BIGINT NOT NULL,
                              CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Crear índices para mejorar el rendimiento de las consultas frecuentes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);