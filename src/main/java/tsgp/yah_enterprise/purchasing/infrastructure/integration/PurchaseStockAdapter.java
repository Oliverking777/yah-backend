package tsgp.yah_enterprise.purchasing.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.domain.exception.PurchaseStockRejectedException;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseStockPort;
import tsgp.yah_enterprise.inventory.application.dto.RecordStockMovementCommand;
import tsgp.yah_enterprise.inventory.application.port.in.RecordStockMovementUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.InventoryDomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Purchasing adds stock through Inventory's inbound port only. The PURCHASE movement joins the caller's transaction,
 * so a refused movement rolls the whole confirmation back. The reference is the purchase, so recording the same
 * confirmation twice is applied once. Inventory's exceptions never escape this class.
 */
@Component
@RequiredArgsConstructor
public class PurchaseStockAdapter implements PurchaseStockPort {

    static final String MOVEMENT_TYPE = "PURCHASE";
    static final String REFERENCE_TYPE = "PURCHASE";

    private final RecordStockMovementUseCase recordStockMovementUseCase;

    @Override
    public void receive(UUID productId, UUID locationId, long baseQuantity, UUID purchaseId, UUID userId,
                        Instant occurredAt) {
        try {
            recordStockMovementUseCase.recordMovement(new RecordStockMovementCommand(productId, locationId,
                    MOVEMENT_TYPE, baseQuantity, REFERENCE_TYPE, purchaseId, userId, occurredAt,
                    "Purchase " + purchaseId));
        } catch (InventoryDomainException e) {
            throw new PurchaseStockRejectedException(e.getMessage());
        }
    }
}
