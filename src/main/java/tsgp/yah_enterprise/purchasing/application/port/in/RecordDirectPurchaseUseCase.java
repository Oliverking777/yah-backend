package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.application.dto.DirectPurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseResult;

/**
 * Goods bought and received on the spot: creates the purchase and confirms it in one transaction, so the kept goods
 * are in stock when it returns.
 */
public interface RecordDirectPurchaseUseCase {
    PurchaseResult recordDirectPurchase(DirectPurchaseCommand command);
}
