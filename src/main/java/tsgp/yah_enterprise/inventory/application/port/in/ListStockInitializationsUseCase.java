package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageResult;

public interface ListStockInitializationsUseCase {

    StockInitializationPageResult listInitializations(StockInitializationPageRequest request);
}
