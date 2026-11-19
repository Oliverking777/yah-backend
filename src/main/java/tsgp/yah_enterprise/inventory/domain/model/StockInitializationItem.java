package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;

import java.util.Objects;
import java.util.UUID;

/**
 * One product line in a stock initialization.
 * <p>
 * countedQuantity is the physical count in the selected unit (e.g. 50 cartons).
 * conversionFactor is the factor of that unit relative to the base unit (e.g. 24 pieces per carton).
 * baseQuantity is the counted quantity expressed in base units (countedQuantity * conversionFactor).
 * The ledger always stores base units; this is what is posted to {@code StockLedger}.
 */
public class StockInitializationItem {

    private final UUID itemId;
    private final UUID productId;
    private final UUID unitId;
    private final int conversionFactor;
    private final long countedQuantity;
    private final long baseQuantity;

    private StockInitializationItem(UUID itemId, UUID productId, UUID unitId, int conversionFactor,
                                    long countedQuantity, long baseQuantity) {
        this.itemId = Objects.requireNonNull(itemId, "itemId must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        if (conversionFactor < 1) {
            throw new InvalidStockInitializationException("Conversion factor must be at least 1");
        }
        this.conversionFactor = conversionFactor;
        if (countedQuantity < 1) {
            throw new InvalidStockInitializationException("Counted quantity must be at least 1");
        }
        this.countedQuantity = countedQuantity;
        if (baseQuantity != countedQuantity * conversionFactor) {
            throw new InvalidStockInitializationException(
                    "Base quantity must equal counted quantity times the conversion factor");
        }
        if (baseQuantity < 1) {
            throw new InvalidStockInitializationException("Base quantity must be at least 1");
        }
        this.baseQuantity = baseQuantity;
    }

    /**
     * Creates a new initialization line.
     *
     * @param conversionFactor factor of the selected unit relative to the base unit (>= 1)
     * @param countedQuantity   physical count in the selected unit (>= 1)
     * @throws InvalidStockInitializationException if any value is invalid
     */
    public static StockInitializationItem create(UUID productId, UUID unitId, int conversionFactor,
                                          long countedQuantity) {
        long baseQty = countedQuantity * conversionFactor;
        return new StockInitializationItem(UUID.randomUUID(), productId, unitId, conversionFactor,
                countedQuantity, baseQty);
    }

    public static StockInitializationItem reconstitute(UUID itemId, UUID productId, UUID unitId, int conversionFactor,
                                                long countedQuantity, long baseQuantity) {
        return new StockInitializationItem(itemId, productId, unitId, conversionFactor,
                countedQuantity, baseQuantity);
    }

    public UUID getItemId() { return itemId; }
    public UUID getProductId() { return productId; }
    public UUID getUnitId() { return unitId; }
    public int getConversionFactor() { return conversionFactor; }
    public long getCountedQuantity() { return countedQuantity; }
    public long getBaseQuantity() { return baseQuantity; }

    @Override public boolean equals(Object o) {
        return o instanceof StockInitializationItem other && itemId.equals(other.itemId);
    }
    @Override public int hashCode() { return itemId.hashCode(); }
}
