package tsgp.yah_enterprise.sales.domain.valueobject;

import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderException;

/** Generated from a sequence (RCT-000001), never edited. */
public record ReceiptNumber(String value) {

    public ReceiptNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderException("ReceiptNumber is required");
        }
    }

    public static ReceiptNumber of(String value) {
        return new ReceiptNumber(value);
    }

    public static ReceiptNumber fromSequence(long seq) {
        return new ReceiptNumber("RCT-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
