package tsgp.yah_enterprise.sales.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.inventory.application.dto.RecordStockMovementCommand;
import tsgp.yah_enterprise.inventory.application.port.in.GetProductStockUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RecordStockMovementUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.InventoryDomainException;
import tsgp.yah_enterprise.sales.domain.exception.OrderStockUnavailableException;
import tsgp.yah_enterprise.sales.domain.port.SalesStockPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Sales reads and writes stock through Inventory's inbound ports only. The SALE movement joins the caller's
 * transaction, so a refused movement rolls the whole completion back. The reference is the order, so recording
 * the same sale twice is applied once. Inventory's exceptions never escape this class.
 */
@Component
@RequiredArgsConstructor
public class SalesStockAdapter implements SalesStockPort {

    static final String REFERENCE_TYPE = "SALE";

    private final GetProductStockUseCase getProductStockUseCase;
    private final RecordStockMovementUseCase recordStockMovementUseCase;

    @Override
    public long availableQuantity(UUID productId, UUID locationId) {
        return getProductStockUseCase.getProductStock(productId, locationId).quantity();
    }

    @Override
    public void deduct(UUID productId, UUID locationId, long baseQuantity, UUID orderId, UUID userId,
                       Instant occurredAt) {
        try {
            recordStockMovementUseCase.recordMovement(new RecordStockMovementCommand(productId, locationId, "SALE",
                    baseQuantity, REFERENCE_TYPE, orderId, userId, occurredAt, null));
        } catch (InventoryDomainException e) {
            throw new OrderStockUnavailableException(e.getMessage());
        }
    }
}
