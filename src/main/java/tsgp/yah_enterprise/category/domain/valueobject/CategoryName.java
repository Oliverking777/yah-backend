package tsgp.yah_enterprise.category.domain.valueobject;

import tsgp.yah_enterprise.category.domain.exception.InvalidCategoryException;

/**
 * Display name of a category. Trimmed, required, at most 100 characters.
 * Names are unique ignoring case: the repository enforces that rule.
 */
public record CategoryName(String value) {

    public static final int MAX_LENGTH = 100;

    public CategoryName {
        if (value == null || value.isBlank()) {
            throw new InvalidCategoryException("Category name is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCategoryException("Category name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static CategoryName of(String value) {
        return new CategoryName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}