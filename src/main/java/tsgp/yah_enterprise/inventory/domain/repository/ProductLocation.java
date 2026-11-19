package tsgp.yah_enterprise.inventory.domain.repository;

import java.util.Objects;
import java.util.UUID;

/** A product at a location: the key of a stock level and of a product's ledger at that location. */
public record ProductLocation(UUID productId, UUID locationId) {
    public ProductLocation {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(locationId, "locationId must not be null");
    }
}