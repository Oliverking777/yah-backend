package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

import java.util.List;
import java.util.UUID;

public interface ListProductPricesUseCase {

    /**
     * Returns the current prices of a product: the ones that are ACTIVE and whose period contains now.
     *
     * @param unitId optional filter; null means every unit
     */
    List<ProductPriceResult> listProductPrices(UUID productId, UUID unitId);
}
