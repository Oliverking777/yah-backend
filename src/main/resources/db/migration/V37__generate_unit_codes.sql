-- Unit codes are now generated: U0001, U0002... Existing codes (PCS, CTN...) are kept: a code never changes.

-- The sequence starts after any code already in the generated format, so none is given twice.
CREATE SEQUENCE IF NOT EXISTS public.unit_code_seq AS BIGINT START WITH 1 INCREMENT BY 1 NO CYCLE;

SELECT setval('public.unit_code_seq',
              COALESCE((SELECT MAX(CAST(SUBSTRING(unit_code FROM 2) AS BIGINT))
                        FROM public.units
                        WHERE unit_code ~ '^U[0-9]{1,9}$'), 0) + 1,
              false);

COMMENT ON SEQUENCE public.unit_code_seq IS 'Numbers of generated unit codes: U0001...';
