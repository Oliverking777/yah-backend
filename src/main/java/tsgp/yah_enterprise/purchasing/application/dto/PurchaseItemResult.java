package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.UUID;

/**
 * A line: what was ordered and, once the purchase is confirmed, what was accepted, rejected and is missing.
 * The delivery figures are null before confirmation.
 */
public record PurchaseItemResult(UUID purchaseItemId, UUID productId, String productName, UUID unitId,
                                 int conversionFactor, int orderedQuantity, long unitPurchasePrice, long lineTotal,
                                 Integer acceptedQuantity, Integer rejectedQuantity, Integer missingQuantity,
                                 String rejectionType, String discrepancyReason) { }
