package tsgp.yah_enterprise.inventory.domain.exception;

/** The user is limited to their own location and asked for another one (or has none). */
public class StockLocationAccessException extends InventoryDomainException {
    public StockLocationAccessException(String message) {
        super(message);
    }
}
