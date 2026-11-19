package tsgp.yah_enterprise.category.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.category.application.dto.CategoryResult;

/**
 * HTTP response body describing one category.
 */
@Schema(description = "A product category")
public record CategoryResponse(

        @Schema(description = "Id of the category", example = "3f1c7a52-9d0e-4b6a-8c21-5e7d9a0b1c34")
        UUID categoryId,

        @Schema(description = "Category name", example = "Spare Parts")
        String categoryName,

        @Schema(description = "Description, if any", example = "Engine and body parts for light vehicles")
        String description,

        @Schema(description = "Whether the category is available", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE"})
        String categoryStatus,

        @Schema(description = "When the category was created (UTC)", example = "2026-10-03T09:15:30Z")
        Instant createdAt,

        @Schema(description = "When the category was last changed (UTC)", example = "2026-10-03T09:15:30Z")
        Instant updatedAt
) {
    public static CategoryResponse from(CategoryResult result) {
        return new CategoryResponse(
                result.categoryId(),
                result.categoryName(),
                result.description(),
                result.categoryStatus(),
                result.createdAt(),
                result.updatedAt());
    }
}