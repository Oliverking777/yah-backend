package tsgp.yah_enterprise.category.application.dto;

/**
 * Input for the CreateCategoryUseCase.
 * Plain strings: the service turns the name into a CategoryName.
 * The description is optional.
 */
public record CreateCategoryCommand(
        String categoryName,
        String description
) {
}