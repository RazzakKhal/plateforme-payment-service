DO $$
BEGIN
    IF to_regclass('public.payment') IS NOT NULL
        AND to_regclass('public.payments') IS NULL THEN
        ALTER TABLE payment RENAME TO payments;
    END IF;
END $$;
