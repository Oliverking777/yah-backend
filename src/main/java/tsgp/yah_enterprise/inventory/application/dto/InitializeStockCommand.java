package tsgp.yah_enterprise.inventory.application.dto;

import java.util.List;
import java.util.UUID;

/** Command to initialize stock for multiple products at one location in a single atomic operation. */
public record InitializeStockCommand(
        UUID locationId,
        List<InitializeStockLineCommand> items,
        UUID requestedBy,
        boolean restrictedToAssignedLocation
) {
}
