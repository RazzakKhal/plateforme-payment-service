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
          AND column_name = 'user_id'
          AND data_type <> 'uuid'
    ) THEN
        IF to_regclass('public.user_id_map') IS NULL THEN
            RAISE EXCEPTION 'Missing user_id_map required to migrate payments.user_id to UUID';
        END IF;

        ALTER TABLE payments ADD COLUMN IF NOT EXISTS new_user_id UUID;
        UPDATE payments p
        SET new_user_id = u.id
        FROM user_id_map u
        WHERE p.user_id IS NOT NULL
          AND p.user_id = u.legacy_id;

        ALTER TABLE payments DROP COLUMN user_id;
        ALTER TABLE payments RENAME COLUMN new_user_id TO user_id;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'payments'
          AND column_name = 'formula_id'
          AND data_type <> 'uuid'
    ) THEN
        IF to_regclass('public.formula_id_map') IS NULL THEN
            RAISE EXCEPTION 'Missing formula_id_map required to migrate payments.formula_id to UUID';
        END IF;

        ALTER TABLE payments ADD COLUMN IF NOT EXISTS new_formula_id UUID;
        UPDATE payments p
        SET new_formula_id = f.id
        FROM formula_id_map f
        WHERE p.formula_id IS NOT NULL
          AND p.formula_id = f.legacy_id;

        ALTER TABLE payments DROP COLUMN formula_id;
        ALTER TABLE payments RENAME COLUMN new_formula_id TO formula_id;
    END IF;
END $$;
