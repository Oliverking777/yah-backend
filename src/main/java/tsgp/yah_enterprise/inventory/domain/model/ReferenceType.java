package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;

/** The kind of document a stock movement points at (referenceId is that document's id). */
public enum ReferenceType {
    PURCHASE, TRANSFER, SALE, ADJUSTMENT, STOCK_INITIALIZATION;

    public static ReferenceType from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockMovementException("Reference type is required");
        }
        try {
            return ReferenceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockMovementException("Unknown reference type: " + value);
        }
    }
}
