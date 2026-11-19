package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.InitializeStockCommand;
import tsgp.yah_enterprise.inventory.application.dto.InitializeStockLineCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Initialize the stock of multiple products at one location in one atomic operation. "
        + "The owner selects a location, adds products with their units and counted quantities, and submits once. "
        + "The system records stock balances and movements for all products or none.")
public record InitializeStockRequest(
        @Schema(description = "Location where the initial stock is recorded", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Location is required")
        UUID locationId,

        @Schema(description = "Product lines to initialize. At least one is required. No duplicate products.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "At least one product is required")
        @Size(max = 100, message = "A single initialization may not contain more than 100 products")
        @Valid
        List<InitializeStockLineRequest> items
) {
    public InitializeStockCommand toCommand(UUID requestedBy, boolean restrictedToAssignedLocation) {
        return new InitializeStockCommand(locationId,
                items.stream().map(InitializeStockLineRequest::toLineCommand).toList(),
                requestedBy, restrictedToAssignedLocation);
    }
}
