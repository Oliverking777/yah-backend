package tsgp.yah_enterprise.category.domain.exception;

import java.util.UUID;

public class CategoryNotFoundException extends CategoryDomainException {
    public CategoryNotFoundException(UUID categoryId) {
        super("Category not found: " + categoryId);
    }
}