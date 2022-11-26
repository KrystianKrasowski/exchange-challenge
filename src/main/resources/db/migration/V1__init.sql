CREATE TABLE user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    pesel VARCHAR(20) NOT NULL
    UNIQUE KEY uq_user_account_pesel (pesel)
)

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(36) PRIMARY KEY,
    account_id BIGINT NOT NULL,
    amount DECIMAL NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (account_id) REFERENCES user_account(id)
    UNIQUE KEY uq_transactions_transactionid_currency (transaction_id, currency)
)
