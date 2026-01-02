-- Accounts table: source of truth for current balance
CREATE TABLE IF NOT EXISTS accounts (
                                        id UUID PRIMARY KEY,
                                        owner TEXT,
                                        status TEXT NOT NULL,
                                        balance_amount NUMERIC(18,2) NOT NULL,
    balance_currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );

-- Operations table: idempotency + audit log (transaction_id is the idempotency key)
CREATE TABLE IF NOT EXISTS operations (
                                          transaction_id UUID PRIMARY KEY,
                                          account_id UUID NOT NULL,
                                          type TEXT NOT NULL,
                                          amount_value NUMERIC(18,2) NOT NULL,
    amount_currency CHAR(3) NOT NULL,
    status TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    resulting_balance_amount NUMERIC(18,2) NOT NULL,
    resulting_balance_currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_operations_account
    FOREIGN KEY (account_id) REFERENCES accounts(id)
    );

CREATE INDEX IF NOT EXISTS idx_operations_account_id
    ON operations(account_id);
