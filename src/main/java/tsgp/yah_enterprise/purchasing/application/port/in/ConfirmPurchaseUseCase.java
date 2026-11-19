package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.application.dto.ConfirmPurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseResult;

/**
 * SUBMITTED to CONFIRMED. One transaction: the delivery on every line, the status change and one PURCHASE stock
 * movement per product for the accepted quantity at the destination. Rejected and missing goods add no stock and set
 * hasDiscrepancy.
 */
public interface ConfirmPurchaseUseCase {
    PurchaseResult confirmPurchase(ConfirmPurchaseCommand command);
}
