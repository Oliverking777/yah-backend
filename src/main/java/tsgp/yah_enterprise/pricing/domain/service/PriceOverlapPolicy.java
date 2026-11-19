package tsgp.yah_enterprise.pricing.domain.service;

import tsgp.yah_enterprise.pricing.domain.exception.PriceRangeOverlapException;
import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;

import java.util.Collection;

/** The engine must never find two applicable prices: this is the rule that prevents it. */
public class PriceOverlapPolicy {

    public void ensureNoOverlap(ProductPrice candidate, Collection<ProductPrice> existingActivePrices) {
        for (ProductPrice existing : existingActivePrices) {
            if (candidate.conflictsWith(existing)) {
                throw new PriceRangeOverlapException(candidate.getQuantityRange());
            }
        }
    }
}
