package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

import java.util.UUID;

public interface DeactivateProductPriceUseCase {

    /**
     * Deactivates a price. The row is kept for the history.
     *
     * @throws tsgp.yah_enterprise.pricing.domain.exception.ProductPriceNotFoundException   if the price does not exist for this product
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceAlreadyInactiveException   if the price is already inactive
     */
    ProductPriceResult deactivateProductPrice(UUID productId, UUID priceId);
}
