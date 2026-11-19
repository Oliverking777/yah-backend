package tsgp.yah_enterprise.purchasing.application.dto;

import tsgp.yah_enterprise.purchasing.domain.model.RejectionType;

import java.util.UUID;

/**
 * One product of a direct purchase. quantityReceived is what is kept: it becomes both the ordered and the accepted
 * quantity. quantityRejected is what was refused at the door (0 or more); it needs a type and a reason.
 */
public record DirectPurchaseLineCommand(UUID productId, UUID unitId, int quantityReceived, long unitPrice,
                                        int quantityRejected, RejectionType rejectionType, String rejectionReason) { }
