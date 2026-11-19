package tsgp.yah_enterprise.pricing.domain.port;

import java.util.UUID;

/** A unit of a product, as the Pricing module sees it. */
public record PriceableUnit(UUID unitId, boolean active) {
}
