package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

/**
 * Name of a variant within its product, e.g. "50 kg 10% broken" for Indian Rice or "50 kg" for Flour.
 * Trimmed, inner spaces collapsed, 1 to 100 characters. Unique within the product ignoring case and spaces, so
 * "50 kg" and "50KG" are the same name. The packaging (bag, carton) is not part of it: that is the variant's unit.
 */
public record VariantName(String value) {

    public static final int MAX_LENGTH = 100;

    public VariantName {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("Variant name is required");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new InvalidProductException("Variant name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static VariantName of(String value) { return new VariantName(value); }

    /** Null or blank means "no name": the default variant of a simple product. */
    public static VariantName ofNullable(String value) {
        return value == null || value.isBlank() ? null : new VariantName(value);
    }

    /** The form compared for uniqueness: lower case, no spaces. */
    public String key() { return value.toLowerCase(java.util.Locale.ROOT).replace(" ", ""); }

    @Override
    public String toString() { return value; }
}