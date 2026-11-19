package tsgp.yah_enterprise.supplier.domain.valueobject;

import tsgp.yah_enterprise.supplier.domain.exception.InvalidSupplierException;

public record SupplierCode(String value) {
    public SupplierCode {
        if (value == null || value.isBlank()) throw new InvalidSupplierException("Supplier code is required");
    }
    public static SupplierCode of(String value) { return new SupplierCode(value); }
    public static SupplierCode fromSequence(long seq) { return new SupplierCode("SUP-" + String.format("%06d", seq)); }
    @Override public String toString() { return value; }
}
