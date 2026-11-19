package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;

/**
 * Status of a stock initialization.
 * COMPLETED — the initialization was submitted and all stock movements were recorded.
 * CANCELLED — reserved for a future cancel feature; no stock was moved.
 */
public enum StockInitializationStatus {

    COMPLETED, CANCELLED;

    public static StockInitializationStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockInitializationException("Stock initialization status is required");
        }
        try {
            return StockInitializationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockInitializationException("Unknown stock initialization status: " + value);
        }
    }
}
