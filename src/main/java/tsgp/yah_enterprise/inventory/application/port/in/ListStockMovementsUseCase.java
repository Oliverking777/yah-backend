package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockMovementPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementPageResult;

public interface ListStockMovementsUseCase {

    /**
     * Lists ledger lines, newest first.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException    if movementType is not a known type
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException     if a limited user asks for another location or has none
     */
    StockMovementPageResult listStockMovements(StockMovementPageRequest request);
}
