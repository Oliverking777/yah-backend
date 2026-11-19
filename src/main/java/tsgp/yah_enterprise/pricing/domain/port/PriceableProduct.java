package tsgp.yah_enterprise.pricing.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** A product, as the Pricing module sees it: only what is needed to decide whether it can be priced. */
public record PriceableProduct(UUID productId, boolean active, List<PriceableUnit> units) {

    public PriceableProduct {
        units = List.copyOf(units);
    }

    public Optional<PriceableUnit> findUnit(UUID unitId) {
        return units.stream().filter(u -> u.unitId().equals(unitId)).findFirst();
    }
}
