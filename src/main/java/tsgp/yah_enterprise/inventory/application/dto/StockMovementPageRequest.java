package tsgp.yah_enterprise.inventory.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Paging and filters of the ledger listing. movementType is a MovementType name or null for any. */
public record StockMovementPageRequest(
        int page,
        int size,
        UUID productId,
        UUID locationId,
        String movementType,
        Instant from,
        Instant to,
        UUID viewerUserId,
        boolean restrictedToAssignedLocation
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public StockMovementPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
