package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationResult;

import java.util.UUID;

public interface GetStockInitializationUseCase {

    /**
     * Retrieve one initialization and its items.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockInitializationNotFoundException if not found
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException         if a limited user targets another location
     */
    StockInitializationResult getInitialization(UUID initializationId, UUID viewerUserId,
                                                 boolean restrictedToAssignedLocation);
}
