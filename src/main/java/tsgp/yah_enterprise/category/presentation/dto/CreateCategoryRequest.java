package tsgp.yah_enterprise.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.category.application.dto.CreateCategoryCommand;

/**
 * HTTP request body for creating a category.
 */
@Schema(description = "Data needed to create a category")
public record CreateCategoryRequest(

        @Schema(description = "Category name. Unique, not case-sensitive.", example = "Spare Parts",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name is too long")
        String categoryName,

        @Schema(description = "Optional description", example = "Engine and body parts for light vehicles")
        @Size(max = 255, message = "Description is too long")
        String description
) {
    public CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(categoryName, description);
    }
}