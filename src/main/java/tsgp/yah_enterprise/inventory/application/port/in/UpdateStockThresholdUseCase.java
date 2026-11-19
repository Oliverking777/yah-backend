package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.ProductStockResult;
import tsgp.yah_enterprise.inventory.application.dto.UpdateStockThresholdCommand;

public interface UpdateStockThresholdUseCase {

    /**
     * Sets or clears the low-stock threshold of a product at a location.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockThresholdException   if the threshold is negative
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException   if the product is missing or inactive
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException  if the location is missing or inactive
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException     if a limited user targets another location
     */
    ProductStockResult updateThreshold(UpdateStockThresholdCommand command);
}
