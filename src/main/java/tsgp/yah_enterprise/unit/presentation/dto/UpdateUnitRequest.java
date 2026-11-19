package tsgp.yah_enterprise.unit.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.unit.application.dto.UpdateUnitCommand;

/**
 * HTTP request body for updating a unit. Only the name can change.
 * The code is permanent and the status is changed with the activate/deactivate endpoints.
 */
@Schema(description = "Fields to change on a unit. Omitted fields stay unchanged.")
public record UpdateUnitRequest(

        @Schema(description = "New unit name", example = "Carton (24)")
        @Size(max = 50, message = "Unit name is too long")
        String unitName
) {
    public UpdateUnitCommand toCommand() {
        return new UpdateUnitCommand(unitName);
    }
}
