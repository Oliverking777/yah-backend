package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/** Used to approve (note optional) or reject (note required) an adjustment. */
public record DecideStockAdjustmentCommand(
        UUID adjustmentId,
        UUID decidedBy,
        String note,
        boolean restrictedToAssignedLocation
) {
}
