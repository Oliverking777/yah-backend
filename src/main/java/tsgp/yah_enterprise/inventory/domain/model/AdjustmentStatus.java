package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException;

public enum AdjustmentStatus {
    PENDING, APPROVED, REJECTED;

    public static AdjustmentStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockAdjustmentException("Adjustment status is required");
        }
        try {
            return AdjustmentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockAdjustmentException("Unknown adjustment status: " + value);
        }
    }
}
