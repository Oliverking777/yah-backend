package tsgp.yah_enterprise.category.application.port.in;

import tsgp.yah_enterprise.category.application.dto.CategoryResult;

import java.util.UUID;

public interface ChangeCategoryStatusUseCase {

    /**
     * Makes a category available again.
     *
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException        if the category does not exist
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyActiveException   if it is already active
     */
    CategoryResult activateCategory(UUID categoryId);

    /**
     * Retires a category. It is kept, not deleted, because products may still reference it.
     *
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException          if the category does not exist
     * @throws tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyInactiveException  if it is already inactive
     */
    CategoryResult deactivateCategory(UUID categoryId);
}