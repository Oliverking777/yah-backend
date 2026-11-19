package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.MovementType;

import java.time.Instant;
import java.util.UUID;

/** Optional filters of a ledger listing. from is inclusive, to is exclusive (on recordedAt). */
public record StockMovementFilter(UUID productId, UUID locationId, MovementType movementType,
                                  Instant from, Instant to) {
}
