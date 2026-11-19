package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockAdjustment;

import java.time.Instant;
import java.util.UUID;

public record StockAdjustmentResult(
        UUID adjustmentId,
        String adjustmentNumber,
        UUID productId,
        String productName,
        UUID locationId,
        String locationName,
        String direction,
        String reasonCategory,
        long quantity,
        String note,
        String adjustmentStatus,
        UUID createdBy,
        UUID decidedBy,
        String decisionNote,
        Long beforeQuantity,
        Long afterQuantity,
        Instant createdAt,
        Instant decidedAt,
        Instant updatedAt
) {
    public static StockAdjustmentResult from(StockAdjustment a, String productName, String locationName) {
        return new StockAdjustmentResult(a.getAdjustmentId(), a.getAdjustmentNumber().value(), a.getProductId(),
                productName, a.getLocationId(), locationName, a.getDirection().name(),
                a.getReasonCategory().name(), a.getQuantity(), a.getNote(), a.getAdjustmentStatus().name(),
                a.getCreatedBy(), a.getDecidedBy(), a.getDecisionNote(), a.getBeforeQuantity(),
                a.getAfterQuantity(), a.getCreatedAt(), a.getDecidedAt(), a.getUpdatedAt());
    }
}
