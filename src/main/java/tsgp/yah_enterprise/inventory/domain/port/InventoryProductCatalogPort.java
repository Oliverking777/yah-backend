package tsgp.yah_enterprise.inventory.domain.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: how Inventory reads the Product module.
 * The adapter lives in infrastructure.integration and calls Product's use cases only.
 */
public interface InventoryProductCatalogPort {

    /** Empty when the product does not exist. */
    Optional<InventoryProduct> findProduct(UUID productId);

    /** Batch lookup for listings. Products that no longer resolve are simply missing from the map. */
    Map<UUID, InventoryProduct> findAllByIds(Collection<UUID> productIds);

    /** Product with its units, for stock initialization. Empty when the product does not exist. */
    Optional<InventoryProductDetail> findProductDetail(UUID productId);
}
