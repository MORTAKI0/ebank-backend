-- T4: add label/motif columns, unique constraints, and indexes (PostgreSQL)

ALTER TABLE bank_account_transactions
    ADD COLUMN IF NOT EXISTS label VARCHAR(255);

ALTER TABLE bank_account_transactions
    ADD COLUMN IF NOT EXISTS motif VARCHAR(500);

UPDATE bank_account_transactions
SET label = 'Transaction'
WHERE label IS NULL;

ALTER TABLE bank_account_transactions
    ALTER COLUMN label SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_customers_identity_ref'
    ) THEN
        ALTER TABLE customers
            ADD CONSTRAINT uk_customers_identity_ref UNIQUE (identity_ref);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_customers_email'
    ) THEN
        ALTER TABLE customers
            ADD CONSTRAINT uk_customers_email UNIQUE (email);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_bank_accounts_rib'
    ) THEN
        ALTER TABLE bank_accounts
            ADD CONSTRAINT uk_bank_accounts_rib UNIQUE (rib);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_customers_identity_ref ON customers (identity_ref);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers (email);
CREATE INDEX IF NOT EXISTS idx_bank_accounts_rib ON bank_accounts (rib);
