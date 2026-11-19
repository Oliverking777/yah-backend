package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

/**
 * A movement would take a stock level below zero. Carries the figures so callers can report them.
 * <p>
 * The message never shows ids: users read it. The ids stay available as fields (for logs). The ledger, which
 * knows the names, replaces the plain message with a named one ({@link #withNames}):
 * "Not enough Indian Rice · 50 kg 10% broken at Boutique Shop: 0 Bag available, 5 Bag needed".
 * Quantities are in base units.
 */
public class InsufficientStockException extends InventoryDomainException {

    private final UUID productId;
    private final UUID locationId;
    private final long available;
    private final long requested;

    public InsufficientStockException(UUID productId, UUID locationId, long available, long requested) {
        this(productId, locationId, available, requested, message(null, null, null, available, requested));
    }

    private InsufficientStockException(UUID productId, UUID locationId, long available, long requested,
                                       String message) {
        super(message);
        this.productId = productId;
        this.locationId = locationId;
        this.available = available;
        this.requested = requested;
    }

    /**
     * The same shortage, told with names. Any name may be null: that part is then left out.
     *
     * @param productName  the full name of the variant, e.g. "Indian Rice · 50 kg 10% broken"
     * @param locationName e.g. "Boutique Shop"
     * @param unitName     the base unit the quantities are counted in, e.g. "Bag"
     */
    public InsufficientStockException withNames(String productName, String locationName, String unitName) {
        return new InsufficientStockException(productId, locationId, available, requested,
                message(productName, locationName, unitName, available, requested));
    }

    private static String message(String productName, String locationName, String unitName,
                                  long available, long requested) {
        String what = isBlank(productName) ? "Not enough stock" : "Not enough " + productName;
        String where = isBlank(locationName) ? "" : " at " + locationName;
        String unit = isBlank(unitName) ? "" : " " + unitName;
        return what + where + ": " + available + unit + " available, " + requested + unit + " needed";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public UUID getProductId() { return productId; }
    public UUID getLocationId() { return locationId; }
    public long getAvailable() { return available; }
    public long getRequested() { return requested; }
}