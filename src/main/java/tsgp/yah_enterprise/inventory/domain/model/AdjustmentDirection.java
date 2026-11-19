package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException;

public enum AdjustmentDirection {
    INCREASE(1), DECREASE(-1);

    private final int sign;

    AdjustmentDirection(int sign) { this.sign = sign; }

    public int sign() { return sign; }

    public static AdjustmentDirection from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockAdjustmentException("Adjustment direction is required (INCREASE or DECREASE)");
        }
        try {
            return AdjustmentDirection.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockAdjustmentException("Unknown adjustment direction: " + value);
        }
    }
}
