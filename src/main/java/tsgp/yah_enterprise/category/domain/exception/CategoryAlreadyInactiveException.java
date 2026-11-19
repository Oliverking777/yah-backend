package tsgp.yah_enterprise.category.domain.exception;

import java.util.UUID;

public class CategoryAlreadyInactiveException extends CategoryDomainException {
    public CategoryAlreadyInactiveException(UUID categoryId) {
        super("Category is already inactive: " + categoryId);
    }
}