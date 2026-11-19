package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.CreateProductPriceCommand;
import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

public interface CreateProductPriceUseCase {

    /**
     * Creates a new, active price. Covers the flat unit price and the quantity-based price.
     *
     * @throws tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException               if the amount or the period is invalid
     * @throws tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException       if the quantity range is invalid
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceProductNotPriceableException   if the product is missing or inactive, or the unit
     *                                                does not belong to it or is inactive
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceRangeOverlapException          if an active price already covers the same quantities
     *                                                in the same period
     */
    ProductPriceResult createProductPrice(CreateProductPriceCommand command);
}
