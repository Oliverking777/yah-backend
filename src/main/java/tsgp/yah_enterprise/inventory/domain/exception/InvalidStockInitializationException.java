package tsgp.yah_enterprise.inventory.domain.exception;

/** Validation failure for a stock initialization (bad quantity, unknown unit, duplicate product...). */
public class InvalidStockInitializationException extends InventoryDomainException {
    public InvalidStockInitializationException(String message) {
        super(message);
    }
}
