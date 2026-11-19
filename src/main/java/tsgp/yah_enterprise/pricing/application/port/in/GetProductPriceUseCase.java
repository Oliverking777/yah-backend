package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

import java.util.UUID;

public interface GetProductPriceUseCase {

    /**
     * @throws tsgp.yah_enterprise.pricing.domain.exception.ProductPriceNotFoundException if the price does not exist for this product
     */
    ProductPriceResult getProductPrice(UUID productId, UUID priceId);
}
