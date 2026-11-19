package tsgp.yah_enterprise.purchasing.application.dto;

import tsgp.yah_enterprise.purchasing.domain.model.PurchaseStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Paging is clamped: page at least 0, size 1 to 100 (default 20). hasDiscrepancy: true for deliveries with rejected
 * or missing goods, false for the others, null for all. from is inclusive and to exclusive, on the order time.
 */
public record PurchasePageRequest(int page, int size, UUID supplierId, UUID locationId, PurchaseStatus status,
                                  Boolean hasDiscrepancy, Instant from, Instant to, PurchaseActor actor) {
    public PurchasePageRequest {
        page = Math.max(page, 0);
        size = size < 1 ? 20 : Math.min(size, 100);
    }
}
