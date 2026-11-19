package tsgp.yah_enterprise.category.domain.exception;

import tsgp.yah_enterprise.category.domain.valueobject.CategoryName;

public class CategoryNameAlreadyExistsException extends CategoryDomainException {
    public CategoryNameAlreadyExistsException(CategoryName name) {
        super("A category named \"" + name.value() + "\" already exists");
    }
}