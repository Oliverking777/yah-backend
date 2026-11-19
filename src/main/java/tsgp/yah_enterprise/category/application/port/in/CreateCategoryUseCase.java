package tsgp.yah_enterprise.category.application.port.in;

import tsgp.yah_enterprise.category.application.dto.CategoryResult;
import tsgp.yah_enterprise.category.application.dto.CreateCategoryCommand;

public interface CreateCategoryUseCase {

    /**
     * Creates a new, active category.
     *
     * @throws tsgp.yah_enterprise.category.domain.exception.InvalidCategoryException              if the name or description is invalid
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNameAlreadyExistsException    if the name is already taken (ignoring case)
     */
    CategoryResult createCategory(CreateCategoryCommand command);
}