package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

/**
 * Barcode printed on a variant (EAN-13, UPC-A, Code 128...). Optional on the variant. Trimmed, 4 to 64 letters or
 * digits (no spaces), kept as typed apart from the trim. Unique across every variant when set.
 */
public record VariantBarcode(String value) {

    private static final String REGEX = "^[A-Za-z0-9]{4,64}$";

    public VariantBarcode {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("Barcode is required");
        }
        value = value.trim();
        if (!value.matches(REGEX)) {
            throw new InvalidProductException("Barcode must be 4 to 64 letters or digits, without spaces");
        }
    }

    /** Null or blank means "no barcode". */
    public static VariantBarcode ofNullable(String value) {
        return value == null || value.isBlank() ? null : new VariantBarcode(value);
    }

    @Override
    public String toString() { return value; }
}