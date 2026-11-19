package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

/**
 * A product has already been initialized at a location and cannot be initialized again.
 * This protects the integrity rule: one initialization per product per location.
 */
public class StockAlreadyInitializedException extends InventoryDomainException {

    private final UUID locationId;
    private final UUID productId;

    public StockAlreadyInitializedException(UUID locationId, UUID productId) {
        super("Product " + productId + " has already been initialized at location " + locationId);
        this.locationId = locationId;
        this.productId = productId;
    }

    public UUID getLocationId() { return locationId; }
    public UUID getProductId() { return productId; }
}
