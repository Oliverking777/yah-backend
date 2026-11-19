package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.ProductStockResult;

import java.util.UUID;

public interface GetProductStockUseCase {

    /** Base units of the product at the location. Zero when nothing was ever recorded; never throws for that. */
    ProductStockResult getProductStock(UUID productId, UUID locationId);
}
