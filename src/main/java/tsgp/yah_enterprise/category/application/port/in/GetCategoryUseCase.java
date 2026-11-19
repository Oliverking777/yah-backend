package tsgp.yah_enterprise.category.application.port.in;

import tsgp.yah_enterprise.category.application.dto.CategoryResult;

import java.util.UUID;

public interface GetCategoryUseCase {

    /**
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException if the category does not exist
     */
    CategoryResult getCategoryById(UUID categoryId);
}