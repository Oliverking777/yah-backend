package tsgp.yah_enterprise.sales.domain.port;

import java.time.Instant;
import java.util.UUID;

/** Sales' view of Inventory. deduct writes a SALE movement in the caller's transaction. */
public interface SalesStockPort {

    long availableQuantity(UUID productId, UUID locationId);

    /** @throws tsgp.yah_enterprise.sales.domain.exception.OrderStockUnavailableException when stock would go below zero */
    void deduct(UUID productId, UUID locationId, long baseQuantity, UUID orderId, UUID userId, Instant occurredAt);
}
