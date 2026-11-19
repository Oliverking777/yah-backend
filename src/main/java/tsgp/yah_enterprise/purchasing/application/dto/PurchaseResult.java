package tsgp.yah_enterprise.purchasing.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * purchaseStatus is SUBMITTED, CONFIRMED or CANCELLED. hasDiscrepancy is the delivery outcome (always false before
 * confirmation). createdBy and createdAt are who ordered and when.
 */
public record PurchaseResult(UUID purchaseId, String purchaseNumber, UUID supplierId, String supplierName,
                             UUID destinationLocationId, String destinationLocationName, String purchaseStatus,
                             boolean hasDiscrepancy, List<PurchaseItemResult> items, long totalAmount,
                             LocalDate expectedDeliveryDate, String note, String deliveryNote, UUID createdBy,
                             UUID confirmedBy, UUID cancelledBy, String cancelReason, Instant createdAt,
                             Instant confirmedAt, Instant cancelledAt, Instant updatedAt) { }
