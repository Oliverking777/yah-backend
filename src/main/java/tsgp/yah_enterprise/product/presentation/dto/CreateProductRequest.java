package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.product.application.dto.CreateProductCommand;

import java.util.List;
import java.util.UUID;

@Schema(description = "A new product with all its variants and their units. The product code and the SKUs are "
        + "generated. One unnamed variant makes a simple product; with several, each needs a name.")
public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String productName,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Public URL of the product image (e.g. a Cloudinary secure_url)")
        @NotBlank(message = "Product image URL is required")
        @Size(max = 2048, message = "Product image URL must not exceed 2048 characters")
        String productImageUrl,

        @NotNull(message = "Category id is required")
        UUID categoryId,

        @NotEmpty(message = "Add at least one variant")
        @Size(max = 50, message = "A product can be created with at most 50 variants")
        List<@Valid @NotNull NewVariantRequest> variants
) {
    public CreateProductCommand toCommand() {
        return new CreateProductCommand(productName, description, productImageUrl, categoryId,
                variants.stream().map(NewVariantRequest::toCommand).toList());
    }
}