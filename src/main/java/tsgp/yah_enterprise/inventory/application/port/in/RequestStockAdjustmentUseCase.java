package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.RequestStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;

public interface RequestStockAdjustmentUseCase {

    /**
     * Records a PENDING adjustment. No stock changes until another user approves it.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException  if quantity, reason, direction or note is invalid
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException   if the product is missing or inactive
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException  if the location is missing or inactive
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException     if a limited user targets another location
     */
    StockAdjustmentResult requestAdjustment(RequestStockAdjustmentCommand command);
}
