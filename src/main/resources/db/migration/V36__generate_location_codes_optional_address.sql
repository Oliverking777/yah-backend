-- Location codes are now generated (LOC-0001, LOC-0002...) and the address is optional.
-- Existing codes (WH-MAIN, ST-890...) are kept: a code never changes.

-- 1. The address may be left empty.
ALTER TABLE public.locations ALTER COLUMN address DROP NOT NULL;

-- 2. The sequence starts after any code already in the generated format, so none is given twice.
CREATE SEQUENCE IF NOT EXISTS public.location_code_seq AS BIGINT START WITH 1 INCREMENT BY 1 NO CYCLE;

SELECT setval('public.location_code_seq',
              COALESCE((SELECT MAX(CAST(SUBSTRING(code FROM 5) AS BIGINT))
                        FROM public.locations
                        WHERE code ~ '^LOC-[0-9]{1,18}$'), 0) + 1,
              false);

COMMENT ON SEQUENCE public.location_code_seq IS 'Numbers of generated location codes: LOC-0001...';
COMMENT ON COLUMN public.locations.address IS 'Physical address; optional';
