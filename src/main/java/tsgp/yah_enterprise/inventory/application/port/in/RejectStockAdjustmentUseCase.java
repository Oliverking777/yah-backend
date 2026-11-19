package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.DecideStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;

public interface RejectStockAdjustmentUseCase {

    /**
     * Rejects a pending adjustment. The note (5 to 500 characters) is the reason. No stock changes.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentNotFoundException             if it does not exist
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentAlreadyDecidedException       if it is not PENDING
     * @throws tsgp.yah_enterprise.inventory.domain.exception.SelfAdjustmentApprovalNotAllowedException    if the decider is the requester
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException              if the reason is missing or too short
     */
    StockAdjustmentResult reject(DecideStockAdjustmentCommand command);
}
