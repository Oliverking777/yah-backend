package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.ApplicablePriceResult;

import java.time.Instant;
import java.util.UUID;

public interface GetApplicablePriceUseCase {

    /**
     * Finds the price that applies to a quantity of one unit at a moment, and the line total.
     * The price of another unit is never converted.
     *
     * @param at the moment to price at; null means now
     * @throws tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException       if the quantity is below 1
     * @throws tsgp.yah_enterprise.pricing.domain.exception.ApplicablePriceNotFoundException    if the unit has no price for this quantity and moment
     */
    ApplicablePriceResult getApplicablePrice(UUID productId, UUID unitId, int quantity, Instant at);
}
