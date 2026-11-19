package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockLevelPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockLevelPageResult;

public interface ListStockLevelsUseCase {

    /**
     * Lists stock levels, optionally only the ones at or below their low-stock threshold.
     * A user limited to one location only ever sees that location.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException if a limited user asks for another location or has none
     */
    StockLevelPageResult listStockLevels(StockLevelPageRequest request);
}
