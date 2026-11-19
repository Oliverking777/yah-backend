package tsgp.yah_enterprise.inventory.domain.exception;

/** Base class of every failure raised by the Inventory module. */
public class InventoryDomainException extends RuntimeException {
    public InventoryDomainException(String message) {
        super(message);
    }
}
