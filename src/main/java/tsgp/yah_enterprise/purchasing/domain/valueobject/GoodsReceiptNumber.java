package tsgp.yah_enterprise.purchasing.domain.valueobject;

import tsgp.yah_enterprise.purchasing.domain.exception.InvalidPurchaseException;

/** Generated from a sequence (GRN-000001), never edited. */
public record GoodsReceiptNumber(String value) {

    public GoodsReceiptNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidPurchaseException("GoodsReceiptNumber is required");
        }
    }

    public static GoodsReceiptNumber of(String value) {
        return new GoodsReceiptNumber(value);
    }

    public static GoodsReceiptNumber fromSequence(long seq) {
        return new GoodsReceiptNumber("GRN-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
