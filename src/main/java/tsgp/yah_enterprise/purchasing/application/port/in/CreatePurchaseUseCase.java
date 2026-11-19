package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.application.dto.CreatePurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseResult;

/** Orders goods: the purchase starts SUBMITTED. The same purchaseId by the same user returns the existing purchase. */
public interface CreatePurchaseUseCase {
    PurchaseResult createPurchase(CreatePurchaseCommand command);
}
