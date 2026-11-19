package tsgp.yah_enterprise.category.domain.repository;

import tsgp.yah_enterprise.category.domain.model.Category;
import tsgp.yah_enterprise.category.domain.valueobject.CategoryName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for managing categories.
 * The implementation lives in the infrastructure layer.
 */
public interface CategoryRepository {

    /** Saves a new category or updates an existing one. */
    Category save(Category category);

    Optional<Category> findById(UUID categoryId);

    /** The lookup ignores case. */
    Optional<Category> findByName(CategoryName categoryName);

    /**
     * Returns one page of categories, ordered by name.
     *
     * @param page zero-based page index
     * @param size number of categories per page
     */
    List<Category> findAll(int page, int size);

    /** Returns the total number of categories. */
    long count();

    boolean existsById(UUID categoryId);

    /** The check ignores case: "Spare Parts" and "spare parts" are the same name. */
    boolean existsByName(CategoryName categoryName);
}