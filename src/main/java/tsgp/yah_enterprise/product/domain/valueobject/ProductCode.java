package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

public record ProductCode(String value) {

    private static final String REGEX = "^[A-Z0-9][A-Z0-9_-]{1,29}$";

    public ProductCode {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("Product code is required");
        }
        value = value.trim().toUpperCase();
        if (!value.matches(REGEX)) {
            throw new InvalidProductException(
                    "Product code must match ^[A-Z0-9][A-Z0-9_-]{1,29}$, got: " + value);
        }
    }

    public static ProductCode of(String value) { return new ProductCode(value); }

    @Override
    public String toString() { return value; }
}
