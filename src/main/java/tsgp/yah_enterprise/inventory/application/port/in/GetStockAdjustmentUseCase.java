package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;

import java.util.UUID;

public interface GetStockAdjustmentUseCase {

    /**
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentNotFoundException  if it does not exist
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException      if a limited user reads another location's adjustment
     */
    StockAdjustmentResult getAdjustment(UUID adjustmentId, UUID viewerUserId, boolean restrictedToAssignedLocation);
}
