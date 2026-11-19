package tsgp.yah_enterprise.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Decision note. Optional when approving; required (5 to 500 characters) when rejecting.")
public record DecideStockAdjustmentRequest(
        @Schema(description = "Note or reason", example = "Photo of the damaged carton received")
        @Size(max = 500, message = "Note is too long")
        String note
) {
}
