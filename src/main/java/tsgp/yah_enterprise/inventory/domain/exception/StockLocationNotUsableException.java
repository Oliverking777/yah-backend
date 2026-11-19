package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

public class StockLocationNotUsableException extends InventoryDomainException {
    public StockLocationNotUsableException(UUID locationId, String reason) {
        super("Location " + locationId + " cannot be used: " + reason);
    }
}
