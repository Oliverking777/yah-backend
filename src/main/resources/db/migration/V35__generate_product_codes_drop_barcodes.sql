-- Product codes and SKUs are now generated, and variants no longer have a barcode.
--   product code  PRD-000001, PRD-000002...  from product_code_seq
--   variant SKU   <product code>-01, -02...  (made by the application; existing SKUs are kept)

-- 1. Barcodes are dropped.
ALTER TABLE public.product_variants DROP CONSTRAINT IF EXISTS ck_product_variants_barcode_format;
ALTER TABLE public.product_variants DROP CONSTRAINT IF EXISTS uq_product_variants_barcode;
ALTER TABLE public.product_variants DROP COLUMN IF EXISTS barcode;

-- 2. The sequence starts after any code already in the generated format, so none is given twice.
CREATE SEQUENCE IF NOT EXISTS public.product_code_seq AS BIGINT START WITH 1 INCREMENT BY 1 NO CYCLE;

SELECT setval('public.product_code_seq',
              COALESCE((SELECT MAX(CAST(SUBSTRING(product_code FROM 5) AS BIGINT))
                        FROM public.products
                        WHERE product_code ~ '^PRD-[0-9]{1,18}$'), 0) + 1,
              false);

COMMENT ON SEQUENCE public.product_code_seq IS 'Numbers of generated product codes: PRD-000001...';
