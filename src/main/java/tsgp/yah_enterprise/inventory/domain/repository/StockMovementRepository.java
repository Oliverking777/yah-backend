package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** The ledger. There is no update and no delete: movements are append-only. */
public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    /** The movement already written for this exact document line, used to make retries idempotent. */
    Optional<StockMovement> findByReference(ReferenceType referenceType, UUID referenceId, UUID productId,
                                            UUID locationId, MovementType movementType);

    /** Newest first; page is zero-based. */
    List<StockMovement> findAll(StockMovementFilter filter, int page, int size);

    long count(StockMovementFilter filter);

    /**
     * The product/location pairs, among every combination of the given products and locations, that have at
     * least one movement in the ledger. One query, whatever the number of pairs. Empty sets give an empty result.
     * Used to tell a stock level with real history from one that only carries a threshold.
     */
    Set<ProductLocation> findWithMovements(Set<UUID> productIds, Set<UUID> locationIds);
}
