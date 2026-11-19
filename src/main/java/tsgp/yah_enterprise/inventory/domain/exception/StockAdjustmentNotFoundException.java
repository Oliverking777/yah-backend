package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

public class StockAdjustmentNotFoundException extends InventoryDomainException {
    public StockAdjustmentNotFoundException(UUID adjustmentId) {
        super("Stock adjustment not found: " + adjustmentId);
    }
}
