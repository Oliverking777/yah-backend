package tsgp.yah_enterprise.category.domain.exception;

import java.util.UUID;

public class CategoryAlreadyActiveException extends CategoryDomainException {
    public CategoryAlreadyActiveException(UUID categoryId) {
        super("Category is already active: " + categoryId);
    }
}