package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

/**
 * A product already has stock history at a location (an initialization, purchase, transfer, sale or approved
 * adjustment was recorded there), so it cannot be initialized there: its quantity is corrected with an
 * adjustment instead. A stock level that only carries a low-stock threshold has no history and does not count.
 */
public class StockAlreadyExistsException extends InventoryDomainException {

    private final UUID locationId;
    private final UUID productId;

    public StockAlreadyExistsException(UUID locationId, UUID productId, String productName, String locationName) {
        super(describe(productId, productName) + " already has stock at " + describeLocation(locationId, locationName)
                + ". Correct its quantity with a stock adjustment instead.");
        this.locationId = locationId;
        this.productId = productId;
    }

    private static String describe(UUID productId, String productName) {
        return productName == null || productName.isBlank() ? "Product " + productId : productName;
    }

    private static String describeLocation(UUID locationId, String locationName) {
        return locationName == null || locationName.isBlank() ? "location " + locationId : locationName;
    }

    public UUID getLocationId() { return locationId; }
    public UUID getProductId() { return productId; }
}