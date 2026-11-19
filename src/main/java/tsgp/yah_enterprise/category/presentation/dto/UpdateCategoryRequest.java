package tsgp.yah_enterprise.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.category.application.dto.UpdateCategoryCommand;

/**
 * HTTP request body for updating a category. Fields that are left out stay unchanged.
 * The status is changed with the activate/deactivate endpoints.
 */
@Schema(description = "Fields to change on a category. Omitted fields stay unchanged.")
public record UpdateCategoryRequest(

        @Schema(description = "New category name", example = "Car Parts")
        @Size(max = 100, message = "Category name is too long")
        String categoryName,

        @Schema(description = "New description. Send an empty string to clear it.", example = "Parts for cars")
        @Size(max = 255, message = "Description is too long")
        String description
) {
    public UpdateCategoryCommand toCommand() {
        return new UpdateCategoryCommand(categoryName, description);
    }
}