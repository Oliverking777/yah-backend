package tsgp.yah_enterprise.supplier.domain.valueobject;

import tsgp.yah_enterprise.supplier.domain.exception.InvalidSupplierException;

public record SupplierName(String value) {
    public static final int MAX_LENGTH = 150;
    public SupplierName {
        if (value == null || value.isBlank()) throw new InvalidSupplierException("Supplier name is required");
        value = value.trim();
        if (value.length() > MAX_LENGTH) throw new InvalidSupplierException("Supplier name must not exceed " + MAX_LENGTH + " characters");
    }
    public static SupplierName of(String value) { return new SupplierName(value); }
    @Override public String toString() { return value; }
}
