DO $$
DECLARE
    user_id_data_type TEXT;
    formula_id_data_type TEXT;
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
        ALTER TABLE payments ADD COLUMN IF NOT EXISTS new_user_id UUID;

        SELECT data_type
        INTO user_id_data_type
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'payments'
          AND column_name = 'user_id';

        IF to_regclass('public.user_id_map') IS NOT NULL THEN
            UPDATE payments p
            SET new_user_id = u.id
            FROM user_id_map u
            WHERE p.user_id IS NOT NULL
              AND p.user_id = u.legacy_id;
        ELSIF user_id_data_type IN ('character varying', 'character', 'text') THEN
            UPDATE payments
            SET new_user_id = NULLIF(BTRIM(user_id::TEXT), '')::UUID
            WHERE user_id IS NOT NULL
              AND BTRIM(user_id::TEXT) ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
        ELSE
            RAISE NOTICE 'Skipping legacy payments.user_id backfill because user_id_map is unavailable in payment-service database';
        END IF;

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
        ALTER TABLE payments ADD COLUMN IF NOT EXISTS new_formula_id UUID;

        SELECT data_type
        INTO formula_id_data_type
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'payments'
          AND column_name = 'formula_id';

        IF to_regclass('public.formula_id_map') IS NOT NULL THEN
            UPDATE payments p
            SET new_formula_id = f.id
            FROM formula_id_map f
            WHERE p.formula_id IS NOT NULL
              AND p.formula_id = f.legacy_id;
        ELSIF formula_id_data_type IN ('character varying', 'character', 'text') THEN
            UPDATE payments
            SET new_formula_id = NULLIF(BTRIM(formula_id::TEXT), '')::UUID
            WHERE formula_id IS NOT NULL
              AND BTRIM(formula_id::TEXT) ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
        ELSE
            RAISE NOTICE 'Skipping legacy payments.formula_id backfill because formula_id_map is unavailable in payment-service database';
        END IF;

        ALTER TABLE payments DROP COLUMN formula_id;
        ALTER TABLE payments RENAME COLUMN new_formula_id TO formula_id;
    END IF;
END $$;
