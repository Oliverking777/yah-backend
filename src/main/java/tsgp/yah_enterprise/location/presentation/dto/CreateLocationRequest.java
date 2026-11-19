package tsgp.yah_enterprise.location.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.location.application.dto.CreateLocationCommand;

/**
 * HTTP request body for creating a location. The code is not sent: the server generates it (LOC-0001, LOC-0002...).
 */
@Schema(description = "Data needed to create a warehouse or a store")
public record CreateLocationRequest(

        @Schema(description = "Display name", example = "Main Warehouse",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name is too long")
        String name,

        @Schema(description = "Kind of location", example = "WAREHOUSE",
                allowableValues = {"WAREHOUSE", "STORE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Location type is required (WAREHOUSE or STORE)")
        String locationType,

        @Schema(description = "Physical address. Optional.", example = "Rue de la Joie, Bonapriso, Douala",
                maxLength = 500, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 500, message = "Address is too long")
        String address
) {
    public CreateLocationCommand toCommand() {
        return new CreateLocationCommand(name, locationType, address);
    }
}