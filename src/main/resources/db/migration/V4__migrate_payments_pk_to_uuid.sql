CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS payment_id_map
(
    legacy_id BIGINT PRIMARY KEY,
    id        UUID NOT NULL UNIQUE
);

DO $$
BEGIN
    IF to_regclass('public.payments') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'payments'
          AND column_name = 'id'
          AND data_type <> 'uuid'
    ) THEN
        ALTER TABLE payments ADD COLUMN IF NOT EXISTS legacy_id BIGINT;
        UPDATE payments SET legacy_id = id WHERE legacy_id IS NULL;

        ALTER TABLE payments ADD COLUMN IF NOT EXISTS new_id UUID;
        UPDATE payments SET new_id = gen_random_uuid() WHERE new_id IS NULL;

        INSERT INTO payment_id_map (legacy_id, id)
        SELECT legacy_id, new_id
        FROM payments
        ON CONFLICT (legacy_id) DO UPDATE SET id = EXCLUDED.id;

        ALTER TABLE payments DROP CONSTRAINT IF EXISTS payment_pkey;
        ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_pkey;
        ALTER TABLE payments DROP COLUMN id;
        ALTER TABLE payments RENAME COLUMN new_id TO id;
        ALTER TABLE payments ADD CONSTRAINT payments_pkey PRIMARY KEY (id);
    END IF;
END $$;
