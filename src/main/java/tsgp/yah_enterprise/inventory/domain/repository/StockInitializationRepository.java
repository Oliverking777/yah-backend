package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.StockInitialization;
import tsgp.yah_enterprise.inventory.domain.valueobject.InitializationNumber;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface StockInitializationRepository {

    StockInitialization save(StockInitialization initialization);

    Optional<StockInitialization> findById(UUID initializationId);

    /** Same as findById, but locks the root row. */
    Optional<StockInitialization> findByIdForUpdate(UUID initializationId);

    /** Next human number from the database sequence (INIT-000001...). */
    InitializationNumber nextNumber();

    /** Newest first; page is zero-based. */
    List<StockInitialization> findAll(StockInitializationFilter filter, int page, int size);

    long count(StockInitializationFilter filter);

    /**
     * Product IDs from the given set that have already been initialized at the given location.
     * Used to enforce the one-initialization-per-product-per-location rule.
     */
    Set<UUID> findInitializedProductIds(UUID locationId, Set<UUID> productIds);
}
