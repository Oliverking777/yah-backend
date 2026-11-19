package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

public class StockProductNotUsableException extends InventoryDomainException {
    public StockProductNotUsableException(UUID productId, String reason) {
        super("Product " + productId + " cannot be used: " + reason);
    }
}
