package tsgp.yah_enterprise.transfer.application.dto;

import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Paging is clamped: page at least 0, size 1 to 100 (default 20). from is inclusive, to is exclusive.
 * hasDiscrepancy: true for receipts with damaged or missing goods, false for the others, null for all.
 */
public record TransferPageRequest(int page, int size, UUID locationId, TransferStatus status,
                                  Boolean hasDiscrepancy, UUID productId, Instant from, Instant to,
                                  TransferActor actor) {

    public TransferPageRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}