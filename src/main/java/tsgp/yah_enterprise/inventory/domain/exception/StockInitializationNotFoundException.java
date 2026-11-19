package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

public class StockInitializationNotFoundException extends InventoryDomainException {
    public StockInitializationNotFoundException(UUID initializationId) {
        super("Stock initialization " + initializationId + " was not found");
    }
}
