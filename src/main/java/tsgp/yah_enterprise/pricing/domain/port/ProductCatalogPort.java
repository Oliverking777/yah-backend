package tsgp.yah_enterprise.pricing.domain.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: how Pricing reads the Product module.
 * The adapter lives in infrastructure.integration and calls Product's use cases only.
 */
public interface ProductCatalogPort {

    /** Empty when the product does not exist. */
    Optional<PriceableProduct> findProduct(UUID productId);
}
