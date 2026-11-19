package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

/**
 * Stock keeping unit code of a variant, e.g. RICE-IND-50KG. Same format as a product code: trimmed, upper-cased,
 * 2 to 30 letters, digits, '-' or '_', starting with a letter or digit. Unique across every variant and never
 * changed after the variant is created.
 */
public record VariantSku(String value) {

    private static final String REGEX = "^[A-Z0-9][A-Z0-9_-]{1,29}$";

    public VariantSku {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("SKU is required");
        }
        value = value.trim().toUpperCase();
        if (!value.matches(REGEX)) {
            throw new InvalidProductException(
                    "SKU must be 2 to 30 letters, digits, '-' or '_', starting with a letter or digit, got: " + value);
        }
    }

    public static VariantSku of(String value) { return new VariantSku(value); }

    @Override
    public String toString() { return value; }
}