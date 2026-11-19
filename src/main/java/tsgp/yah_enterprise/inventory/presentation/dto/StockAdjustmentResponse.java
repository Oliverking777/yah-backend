package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A stock adjustment")
public record StockAdjustmentResponse(UUID adjustmentId, String adjustmentNumber, UUID productId,
                                      String productName, UUID locationId, String locationName,
                                      String direction, String reasonCategory, long quantity, String note,
                                      String adjustmentStatus, UUID createdBy, UUID decidedBy,
                                      String decisionNote, Long beforeQuantity, Long afterQuantity,
                                      Instant createdAt, Instant decidedAt, Instant updatedAt) {
    public static StockAdjustmentResponse from(StockAdjustmentResult r) {
        return new StockAdjustmentResponse(r.adjustmentId(), r.adjustmentNumber(), r.productId(),
                r.productName(), r.locationId(), r.locationName(), r.direction(), r.reasonCategory(),
                r.quantity(), r.note(), r.adjustmentStatus(), r.createdBy(), r.decidedBy(), r.decisionNote(),
                r.beforeQuantity(), r.afterQuantity(), r.createdAt(), r.decidedAt(), r.updatedAt());
    }
}
