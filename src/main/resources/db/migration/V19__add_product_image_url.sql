-- =============================================================
-- V19 – Add the required product image URL to products
-- =============================================================
-- The image itself is hosted on Cloudinary (uploaded by the frontend);
-- the backend stores only its public URL.
--
-- The column is NOT NULL. Rows that already exist get a placeholder URL
-- through a temporary DEFAULT, which is dropped right after so that every
-- new row must supply its own value. Replace the placeholder for existing
-- products with their real image URLs when available.
-- =============================================================

ALTER TABLE public.products
    ADD COLUMN product_image_url VARCHAR(2048) NOT NULL
        DEFAULT 'https://placehold.co/600x600?text=No+Image';

ALTER TABLE public.products
    ALTER COLUMN product_image_url DROP DEFAULT;

ALTER TABLE public.products
    ADD CONSTRAINT ck_products_image_url_not_blank CHECK (TRIM(product_image_url) <> '');

COMMENT ON COLUMN public.products.product_image_url IS 'Public http(s) URL of the product image (hosted on Cloudinary). Required.';
