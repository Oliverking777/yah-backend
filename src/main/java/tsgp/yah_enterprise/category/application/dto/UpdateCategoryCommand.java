package tsgp.yah_enterprise.category.application.dto;

/**
 * Input for the UpdateCategoryUseCase.
 * A field left null keeps its current value.
 * To clear the description, send a blank one (for example "").
 * The status is changed with activate/deactivate, not here.
 */
public record UpdateCategoryCommand(
        String categoryName,
        String description
) {
}