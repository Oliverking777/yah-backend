package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

public record ProductName(String value) {

    public static final int MAX_LENGTH = 150;

    public ProductName {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("Product name is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidProductException("Product name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static ProductName of(String value) { return new ProductName(value); }

    @Override
    public String toString() { return value; }
}
