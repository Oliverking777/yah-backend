package tsgp.yah_enterprise.category.application.port.in;

import tsgp.yah_enterprise.category.application.dto.CategoryResult;
import tsgp.yah_enterprise.category.application.dto.UpdateCategoryCommand;

import java.util.UUID;

public interface UpdateCategoryUseCase {

    /**
     * Updates the name and/or description of a category. Fields left null are unchanged;
     * a blank description clears it. The status is not changed here.
     *
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException             if the category does not exist
     * @throws tsgp.yah_enterprise.category.domain.exception.InvalidCategoryException              if a value is invalid
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNameAlreadyExistsException    if another category already uses the name
     */
    CategoryResult updateCategory(UUID categoryId, UpdateCategoryCommand command);
}