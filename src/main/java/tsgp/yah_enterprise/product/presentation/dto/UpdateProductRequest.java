package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.product.application.dto.UpdateProductCommand;

@Schema(description = "Fields to update on a product. Omitted fields are unchanged.")
public record UpdateProductRequest(
        @Size(max = 150) String productName,
        @Size(max = 500) String description,
        @Schema(description = "New public URL of the product image (e.g. a Cloudinary secure_url)")
        @Size(max = 2048) String productImageUrl
) {
    public UpdateProductCommand toCommand() {
        return new UpdateProductCommand(productName, description, productImageUrl);
    }
}