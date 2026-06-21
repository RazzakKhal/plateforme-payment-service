ALTER TABLE public.payments
    DROP CONSTRAINT IF EXISTS payment_status_check;

ALTER TABLE public.payments
    ADD CONSTRAINT payment_status_check
        CHECK (((status)::TEXT = ANY ((ARRAY[
            'PENDING'::VARCHAR,
            'SUCCESS'::VARCHAR,
            'FAILED'::VARCHAR,
            'INVALID_SIGNATURE'::VARCHAR,
            'COMPLETED'::VARCHAR
        ])::TEXT[])));
