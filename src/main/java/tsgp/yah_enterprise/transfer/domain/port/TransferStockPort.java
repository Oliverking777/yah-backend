package tsgp.yah_enterprise.transfer.domain.port;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound: writes stock through the Inventory module. This is the only way Transfer changes stock.
 * Inventory's own exceptions never escape: an insufficient stock becomes TransferStockUnavailableException.
 */
public interface TransferStockPort {

    enum Direction { OUT, IN }

    /** Moves baseQuantity of a product at a location. The reference is this transfer, so a retry is idempotent. */
    void move(UUID productId, UUID locationId, Direction direction, long baseQuantity, UUID transferId,
              UUID userId, Instant occurredAt);
}
