package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockInitialization;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Result of a stock initialization: the aggregate plus enriched names for products, location and units. */
public record StockInitializationResult(
        UUID initializationId,
        String initializationNumber,
        UUID locationId,
        String locationName,
        String status,
        UUID initializedBy,
        Instant createdAt,
        Instant completedAt,
        List<StockInitializationItemResult> items
) {
    public static StockInitializationResult from(StockInitialization init, String locationName,
                                                 List<StockInitializationItemResult> items) {
        return new StockInitializationResult(init.getInitializationId(),
                init.getInitializationNumber().value(), init.getLocationId(), locationName,
                init.getStatus().name(), init.getInitializedBy(), init.getCreatedAt(),
                init.getCompletedAt(), items);
    }
}
