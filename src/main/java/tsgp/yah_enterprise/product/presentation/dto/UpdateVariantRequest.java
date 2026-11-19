package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.product.application.dto.UpdateVariantCommand;

@Schema(description = "Renames a variant. Empty removes the name, only while it is the product's only variant. "
        + "The SKU never changes.")
public record UpdateVariantRequest(
        @Size(max = 100, message = "Variant name must not exceed 100 characters") String variantName
) {
    public UpdateVariantCommand toCommand() {
        return new UpdateVariantCommand(variantName);
    }
}