package tsgp.yah_enterprise.pricing.domain.service;

import tsgp.yah_enterprise.pricing.domain.exception.ApplicablePriceNotFoundException;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException;
import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Picks the single price for a product, unit, quantity and moment. Never converts between units. */
public class PriceResolver {

    public ProductPrice resolve(UUID productId, UUID unitId, Collection<ProductPrice> prices,
                                int quantity, Instant at) {
        if (quantity < 1) {
            throw new InvalidQuantityRangeException("Quantity must be at least 1");
        }
        List<ProductPrice> matches = prices.stream()
                .filter(ProductPrice::isActive)
                .filter(p -> p.getProductId().equals(productId) && p.getUnitId().equals(unitId))
                .filter(p -> p.getQuantityRange().contains(quantity))
                .filter(p -> p.isEffectiveAt(at))
                .toList();
        if (matches.isEmpty()) {
            throw new ApplicablePriceNotFoundException(productId, unitId, quantity);
        }
        if (matches.size() > 1) {
            // Cannot happen while the overlap rule and the database constraint hold.
            throw new IllegalStateException("More than one applicable price found for product " + productId);
        }
        return matches.get(0);
    }
}
