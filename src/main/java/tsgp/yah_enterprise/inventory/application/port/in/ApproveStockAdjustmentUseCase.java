package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.DecideStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;

public interface ApproveStockAdjustmentUseCase {

    /**
     * Approves a pending adjustment and moves the stock in the same transaction.
     * If the stock is not enough the adjustment stays PENDING.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentNotFoundException             if it does not exist
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentAlreadyDecidedException       if it is not PENDING
     * @throws tsgp.yah_enterprise.inventory.domain.exception.SelfAdjustmentApprovalNotAllowedException    if the approver is the requester
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InsufficientStockException                   if a decrease exceeds the stock
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException                 if a limited user decides another location
     */
    StockAdjustmentResult approve(DecideStockAdjustmentCommand command);
}
