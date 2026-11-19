package tsgp.yah_enterprise.location.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.location.application.dto.UpdateLocationCommand;

/**
 * HTTP request body for updating a location. Fields that are left out stay unchanged.
 * The code cannot be changed, and the status is changed with the activate/deactivate endpoints.
 */
@Schema(description = "Fields to change on a location. Omitted fields stay unchanged.")
public record UpdateLocationRequest(

        @Schema(description = "New display name", example = "Central Warehouse")
        @Size(max = 150, message = "Name is too long")
        String name,

        @Schema(description = "New kind of location", example = "STORE", allowableValues = {"WAREHOUSE", "STORE"})
        String locationType,

        @Schema(description = "New physical address. Send an empty text to remove it.", example = "Avenue Kennedy, Yaoundé")
        @Size(max = 500, message = "Address is too long")
        String address
) {
    public UpdateLocationCommand toCommand() {
        return new UpdateLocationCommand(name, locationType, address);
    }
}