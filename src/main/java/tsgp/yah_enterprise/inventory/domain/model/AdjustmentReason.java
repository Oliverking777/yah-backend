package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException;

/**
 * Reason category of a stock adjustment (FR-041). Every adjustment must have one.
 * DAMAGE, LOSS and SUPPLIER_RETURN only remove stock; OPENING_STOCK only adds it (it is how the
 * first quantity of a product enters a location); COUNT_CORRECTION and OTHER may do either.
 */
public enum AdjustmentReason {
    DAMAGE(AdjustmentDirection.DECREASE),
    LOSS(AdjustmentDirection.DECREASE),
    SUPPLIER_RETURN(AdjustmentDirection.DECREASE),
    OPENING_STOCK(AdjustmentDirection.INCREASE),
    COUNT_CORRECTION(null),
    OTHER(null);

    private final AdjustmentDirection onlyDirection;

    AdjustmentReason(AdjustmentDirection onlyDirection) {
        this.onlyDirection = onlyDirection;
    }

    public boolean allows(AdjustmentDirection direction) {
        return onlyDirection == null || onlyDirection == direction;
    }

    public static AdjustmentReason from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockAdjustmentException("Adjustment reason category is required");
        }
        try {
            return AdjustmentReason.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockAdjustmentException("Unknown adjustment reason: " + value);
        }
    }
}
