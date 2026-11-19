package tsgp.yah_enterprise.purchasing.domain.port;

import java.time.Instant;
import java.util.UUID;

/** Purchasing's view of Inventory. receive adds stock in the caller's transaction. */
public interface PurchaseStockPort {

    /**
     * Adds the accepted goods of a confirmed purchase. The purchase is the reference: one movement per product.
     * @throws tsgp.yah_enterprise.purchasing.domain.exception.PurchaseStockRejectedException when Inventory refuses the movement
     */
    void receive(UUID productId, UUID locationId, long baseQuantity, UUID purchaseId, UUID userId,
                 Instant occurredAt);
}
