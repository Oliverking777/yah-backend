package tsgp.yah_enterprise.purchasing.domain.model;

import java.util.UUID;

/**
 * What arrived for one purchase line, in the line's unit. accepted goes into stock; rejected does not.
 * rejectionType is required when something is rejected; reason is required when the line differs from what was
 * ordered (rejected goods or a shortfall).
 */
public record ReceiptLine(UUID purchaseItemId, int accepted, int rejected, RejectionType rejectionType,
                          String reason) { }
