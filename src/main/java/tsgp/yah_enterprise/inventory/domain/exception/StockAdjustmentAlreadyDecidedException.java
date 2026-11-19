package tsgp.yah_enterprise.inventory.domain.exception;

import java.util.UUID;

public class StockAdjustmentAlreadyDecidedException extends InventoryDomainException {
    public StockAdjustmentAlreadyDecidedException(UUID adjustmentId, String status) {
        super("Stock adjustment " + adjustmentId + " is already " + status);
    }
}
