package tsgp.yah_enterprise.category.application.port.in;

import tsgp.yah_enterprise.category.application.dto.CategoryPageRequest;
import tsgp.yah_enterprise.category.application.dto.CategoryPageResult;

public interface ListCategoriesUseCase {

    /**
     * Returns one page of categories, ordered by name.
     */
    CategoryPageResult listCategories(CategoryPageRequest request);
}