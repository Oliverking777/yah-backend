package tsgp.yah_enterprise.unit.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.unit.application.dto.CreateUnitCommand;

/** HTTP request body for creating a unit. The code is not sent: the server generates it (U0001, U0002...). */
@Schema(description = "Data needed to create a unit")
public record CreateUnitRequest(

        @Schema(description = "Unit name. Unique, not case-sensitive.", example = "Carton",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Unit name is required")
        @Size(max = 50, message = "Unit name is too long")
        String unitName
) {
    public CreateUnitCommand toCommand() {
        return new CreateUnitCommand(unitName);
    }
}