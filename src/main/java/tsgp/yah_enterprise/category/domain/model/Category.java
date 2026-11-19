package tsgp.yah_enterprise.category.domain.model;

import tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyActiveException;
import tsgp.yah_enterprise.category.domain.exception.CategoryAlreadyInactiveException;
import tsgp.yah_enterprise.category.domain.exception.InvalidCategoryException;
import tsgp.yah_enterprise.category.domain.valueobject.CategoryName;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * A product category. Aggregate root of the Category module.
 * Products live in the Catalog module and reference a category by id only.
 */

@Getter
public class Category {

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final UUID categoryId;
    private CategoryName categoryName;
    private String description;
    private CategoryStatus categoryStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private Category(UUID categoryId, CategoryName categoryName, String description,
                     CategoryStatus categoryStatus, Instant createdAt, Instant updatedAt) {
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.categoryName = Objects.requireNonNull(categoryName, "categoryName must not be null");
        this.description = validateDescription(description);
        this.categoryStatus = Objects.requireNonNull(categoryStatus, "categoryStatus must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Creates a brand new, active category with a freshly generated id.
     *
     * @throws InvalidCategoryException if the name or description is invalid
     */
    public static Category create(String categoryName, String description) {
        Instant now = Instant.now();
        return new Category(UUID.randomUUID(), CategoryName.of(categoryName), description,
                CategoryStatus.ACTIVE, now, now);
    }

    /** Rebuilds an existing category from stored data (e.g. from the database). */
    public static Category reconstitute(UUID categoryId, String categoryName, String description,
                                        CategoryStatus categoryStatus, Instant createdAt, Instant updatedAt) {
        return new Category(categoryId, CategoryName.of(categoryName), description,
                categoryStatus, createdAt, updatedAt);
    }

    // ---- behaviour ----

    public void changeName(String newName) {
        this.categoryName = CategoryName.of(newName);
        touch();
    }

    /** A null or blank description clears it. */
    public void changeDescription(String newDescription) {
        this.description = validateDescription(newDescription);
        touch();
    }

    public void activate() {
        if (categoryStatus == CategoryStatus.ACTIVE) {
            throw new CategoryAlreadyActiveException(categoryId);
        }
        this.categoryStatus = CategoryStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        if (categoryStatus == CategoryStatus.INACTIVE) {
            throw new CategoryAlreadyInactiveException(categoryId);
        }
        this.categoryStatus = CategoryStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return categoryStatus == CategoryStatus.ACTIVE;
    }

    
    // ---- helpers ----

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String validateDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidCategoryException(
                    "Category description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Category other && categoryId.equals(other.categoryId));
    }

    @Override
    public int hashCode() {
        return categoryId.hashCode();
    }

    @Override
    public String toString() {
        return "Category{categoryId=" + categoryId + ", categoryName=" + categoryName + "}";
    }
}