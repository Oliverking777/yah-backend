package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.application.dto.PurchaseReasonCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseResult;

/** SUBMITTED to CANCELLED, before anything arrived. A reason is required. */
public interface CancelPurchaseUseCase {
    PurchaseResult cancelPurchase(PurchaseReasonCommand command);
}
