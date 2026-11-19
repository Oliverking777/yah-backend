package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A stock initialization and its product lines")
public record StockInitializationResponse(
        @Schema(description = "Initialization id") UUID initializationId,
        @Schema(description = "Human number (INIT-000001)") String initializationNumber,
        @Schema(description = "Location id") UUID locationId,
        @Schema(description = "Location name") String locationName,
        @Schema(description = "Status (COMPLETED or CANCELLED)") String status,
        @Schema(description = "User who performed the initialization") UUID initializedBy,
        @Schema(description = "When the initialization was created") Instant createdAt,
        @Schema(description = "When the initialization was completed") Instant completedAt,
        @Schema(description = "Product lines") List<StockInitializationItemResponse> items
) {
    public static StockInitializationResponse from(StockInitializationResult r) {
        return new StockInitializationResponse(r.initializationId(), r.initializationNumber(),
                r.locationId(), r.locationName(), r.status(), r.initializedBy(),
                r.createdAt(), r.completedAt(),
                r.items().stream().map(StockInitializationItemResponse::from).toList());
    }
}
