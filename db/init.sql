-- Shared schema for the mini-settlement-system demo.
-- All three services point at the same database, but each one only
-- touches the columns it actually needs (see each service's entity class).
CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    reference   VARCHAR(64) NOT NULL UNIQUE,
    amount      NUMERIC(18, 2) NOT NULL,
    currency    VARCHAR(3) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    settled_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions (status);
CREATE INDEX IF NOT EXISTS idx_transactions_settled_at ON transactions (settled_at);
