package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/**
 * Paging and filters of the stock listing. Paging is clamped, not rejected.
 * restrictedToAssignedLocation comes from the caller's role: true means the user may only see their own location.
 */
public record StockLevelPageRequest(
        int page,
        int size,
        UUID locationId,
        UUID productId,
        boolean lowStockOnly,
        UUID viewerUserId,
        boolean restrictedToAssignedLocation
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public StockLevelPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
