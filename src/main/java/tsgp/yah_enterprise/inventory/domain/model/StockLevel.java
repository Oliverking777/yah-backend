package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InsufficientStockException;
import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockThresholdException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * How many base units of one product are at one location right now (aggregate root).
 * It is a cache of the movement ledger: it only changes together with a stock movement and can
 * never go below zero. Goods in transit are not here (they belong to the Transfer module).
 */
public class StockLevel {

    private final UUID stockLevelId;
    private final UUID productId;
    private final UUID locationId;
    private long quantity;
    private Long lowStockThreshold;
    private final Instant createdAt;
    private Instant updatedAt;

    private StockLevel(UUID stockLevelId, UUID productId, UUID locationId, long quantity,
                       Long lowStockThreshold, Instant createdAt, Instant updatedAt) {
        this.stockLevelId = Objects.requireNonNull(stockLevelId, "stockLevelId must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.locationId = Objects.requireNonNull(locationId, "locationId must not be null");
        if (quantity < 0) {
            throw new IllegalStateException("Stock quantity cannot be negative");
        }
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /** A level that starts at zero. */
    public static StockLevel open(UUID productId, UUID locationId) {
        Instant now = Instant.now();
        return new StockLevel(UUID.randomUUID(), productId, locationId, 0L, null, now, now);
    }

    public static StockLevel reconstitute(UUID stockLevelId, UUID productId, UUID locationId, long quantity,
                                          Long lowStockThreshold, Instant createdAt, Instant updatedAt) {
        return new StockLevel(stockLevelId, productId, locationId, quantity, lowStockThreshold, createdAt, updatedAt);
    }

    /**
     * Adds (positive) or removes (negative) base units.
     *
     * @throws InsufficientStockException if the result would be below zero
     */
    public void apply(long delta) {
        long result = quantity + delta;
        if (result < 0) {
            throw new InsufficientStockException(productId, locationId, quantity, -delta);
        }
        this.quantity = result;
        touch();
    }

    /** Sets the low-stock threshold, or clears it when null. */
    public void changeThreshold(Long threshold) {
        if (threshold != null && threshold < 0) {
            throw new InvalidStockThresholdException("Low-stock threshold must not be negative");
        }
        this.lowStockThreshold = threshold;
        touch();
    }

    /** True when a threshold is set and the quantity has reached it. No threshold means never low. */
    public boolean isLowStock() {
        return lowStockThreshold != null && quantity <= lowStockThreshold;
    }

    private void touch() { this.updatedAt = Instant.now(); }

    public UUID getStockLevelId() { return stockLevelId; }
    public UUID getProductId() { return productId; }
    public UUID getLocationId() { return locationId; }
    public long getQuantity() { return quantity; }
    public Long getLowStockThreshold() { return lowStockThreshold; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override public boolean equals(Object o) {
        return o instanceof StockLevel other && stockLevelId.equals(other.stockLevelId);
    }
    @Override public int hashCode() { return stockLevelId.hashCode(); }
}
