package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/** Paging and filters of the adjustment listing. status is an AdjustmentStatus name or null for any. */
public record StockAdjustmentPageRequest(
        int page,
        int size,
        String status,
        UUID locationId,
        UUID productId,
        UUID viewerUserId,
        boolean restrictedToAssignedLocation
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public StockAdjustmentPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
