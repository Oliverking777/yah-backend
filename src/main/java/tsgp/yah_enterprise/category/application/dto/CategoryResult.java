package tsgp.yah_enterprise.category.application.dto;

import tsgp.yah_enterprise.category.domain.model.Category;

import java.time.Instant;
import java.util.UUID;

/**
 * Output describing a single category.
 */
public record CategoryResult(
        UUID categoryId,
        String categoryName,
        String description,
        String categoryStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResult from(Category category) {
        return new CategoryResult(
                category.getCategoryId(),
                category.getCategoryName().value(),
                category.getDescription(),
                category.getCategoryStatus().toString(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}