package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.StockLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockLevelRepository {

    StockLevel save(StockLevel stockLevel);

    /** Plain read, no lock. Empty when nothing was ever recorded for this product at this location. */
    Optional<StockLevel> findByProductAndLocation(UUID productId, UUID locationId);

    /**
     * Returns the level with its row locked until the transaction ends, creating it at zero if it does not
     * exist yet. Two concurrent callers for the same product and location are serialized here, which is what
     * keeps stock from going below zero and makes retries safe.
     */
    StockLevel lockOrCreate(UUID productId, UUID locationId);

    /** Ordered by product then location; page is zero-based. */
    List<StockLevel> findAll(StockLevelFilter filter, int page, int size);

    long count(StockLevelFilter filter);
}
