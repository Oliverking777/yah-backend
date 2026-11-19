package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageResult;

public interface ListStockAdjustmentsUseCase {

    /**
     * Lists adjustments, newest first.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException  if status is not a known status
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException     if a limited user asks for another location or has none
     */
    StockAdjustmentPageResult listAdjustments(StockAdjustmentPageRequest request);
}
