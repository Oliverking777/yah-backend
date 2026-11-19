package tsgp.yah_enterprise.purchasing.domain.valueobject;

import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchaseException;

/** Generated from a sequence (PUR-000001), never edited. */
public record PurchaseNumber(String value) {

    public PurchaseNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidPurchaseException("PurchaseNumber is required");
        }
    }

    public static PurchaseNumber of(String value) {
        return new PurchaseNumber(value);
    }

    public static PurchaseNumber fromSequence(long seq) {
        return new PurchaseNumber("PUR-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
