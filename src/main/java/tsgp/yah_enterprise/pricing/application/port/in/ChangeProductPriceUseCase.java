package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.ChangeProductPriceCommand;
import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

import java.util.UUID;

public interface ChangeProductPriceUseCase {

    /**
     * Changes the amount and/or the quantity range. History is kept: the old row is closed and a new
     * row is created, and the new row is returned. When nothing would change, the existing row is returned.
     *
     * @throws tsgp.yah_enterprise.pricing.domain.exception.ProductPriceNotFoundException       if the price does not exist for this product
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceAlreadyInactiveException       if the price is already inactive
     * @throws tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException               if nothing was given to change, or the price has expired
     * @throws tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException       if the new range is invalid
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceProductNotPriceableException   if the product or the unit is no longer active
     * @throws tsgp.yah_enterprise.pricing.domain.exception.PriceRangeOverlapException          if the new range clashes with another active price
     */
    ProductPriceResult changeProductPrice(UUID productId, UUID priceId, ChangeProductPriceCommand command);
}
