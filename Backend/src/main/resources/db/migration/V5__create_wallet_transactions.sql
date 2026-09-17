CREATE TABLE wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    job_id BIGINT REFERENCES jobs(id),
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    locked_after DECIMAL(15,2) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallet_transaction_idempotency UNIQUE (wallet_id, idempotency_key)
);

CREATE INDEX idx_wallet_transactions_wallet_created ON wallet_transactions(wallet_id, created_at DESC);
