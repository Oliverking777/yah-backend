package tsgp.yah_enterprise.transfer.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.inventory.application.dto.RecordStockMovementCommand;
import tsgp.yah_enterprise.inventory.application.port.in.RecordStockMovementUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.InventoryDomainException;
import tsgp.yah_enterprise.transfer.domain.exception.TransferStockUnavailableException;
import tsgp.yah_enterprise.transfer.domain.port.TransferStockPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer writes stock through Inventory's inbound port only. It joins the caller's transaction, so a refused
 * movement rolls the whole send, receipt or cancel back. Inventory's exceptions never escape this class.
 * The reference is the transfer, so recording the same send, receipt or cancel twice is applied once.
 */
@Component
@RequiredArgsConstructor
public class TransferStockAdapter implements TransferStockPort {

    static final String REFERENCE_TYPE = "TRANSFER";

    private final RecordStockMovementUseCase recordStockMovementUseCase;

    @Override
    public void move(UUID productId, UUID locationId, Direction direction, long baseQuantity, UUID transferId,
                     UUID userId, Instant occurredAt) {
        String movementType = direction == Direction.OUT ? "TRANSFER_OUT" : "TRANSFER_IN";
        try {
            recordStockMovementUseCase.recordMovement(new RecordStockMovementCommand(
                    productId, locationId, movementType, baseQuantity, REFERENCE_TYPE, transferId,
                    userId, occurredAt, null));
        } catch (InventoryDomainException e) {
            // InsufficientStockException (stock would go below zero) and any other refusal by Inventory
            throw new TransferStockUnavailableException(e.getMessage());
        }
    }
}