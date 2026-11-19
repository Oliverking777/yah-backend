package tsgp.yah_enterprise.category.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.category.application.dto.CategoryPageRequest;
import tsgp.yah_enterprise.category.application.dto.CategoryPageResult;
import tsgp.yah_enterprise.category.application.dto.CategoryResult;
import tsgp.yah_enterprise.category.application.dto.CreateCategoryCommand;
import tsgp.yah_enterprise.category.application.dto.UpdateCategoryCommand;
import tsgp.yah_enterprise.category.application.port.in.ChangeCategoryStatusUseCase;
import tsgp.yah_enterprise.category.application.port.in.CreateCategoryUseCase;
import tsgp.yah_enterprise.category.application.port.in.GetCategoryUseCase;
import tsgp.yah_enterprise.category.application.port.in.ListCategoriesUseCase;
import tsgp.yah_enterprise.category.application.port.in.UpdateCategoryUseCase;
import tsgp.yah_enterprise.category.domain.exception.CategoryNameAlreadyExistsException;
import tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException;
import tsgp.yah_enterprise.category.domain.model.Category;
import tsgp.yah_enterprise.category.domain.repository.CategoryRepository;
import tsgp.yah_enterprise.category.domain.valueobject.CategoryName;

/**
 * Application service implementing all category use cases.
 * Who may call each use case is decided by the web/security layer.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService implements
        CreateCategoryUseCase,
        UpdateCategoryUseCase,
        GetCategoryUseCase,
        ListCategoriesUseCase,
        ChangeCategoryStatusUseCase {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResult createCategory(CreateCategoryCommand command) {
        CategoryName name = CategoryName.of(command.categoryName());

        if (categoryRepository.existsByName(name)) {
            throw new CategoryNameAlreadyExistsException(name);
        }

        Category category = Category.create(command.categoryName(), command.description());
        return CategoryResult.from(categoryRepository.save(category));
    }

    @Override
    public CategoryResult updateCategory(UUID categoryId, UpdateCategoryCommand command) {
        Category category = findCategory(categoryId);

        if (command.categoryName() != null) {
            CategoryName newName = CategoryName.of(command.categoryName());

            // The name may only clash with a *different* category: renaming "parts" to "Parts" is allowed.
            boolean usedByAnother = categoryRepository.findByName(newName)
                    .filter(other -> !other.getCategoryId().equals(categoryId))
                    .isPresent();
            if (usedByAnother) {
                throw new CategoryNameAlreadyExistsException(newName);
            }
            if (!newName.equals(category.getCategoryName())) {
                category.changeName(command.categoryName());
            }
        }

        if (command.description() != null) {
            category.changeDescription(command.description());
        }

        return CategoryResult.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResult getCategoryById(UUID categoryId) {
        return CategoryResult.from(findCategory(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryPageResult listCategories(CategoryPageRequest request) {
        List<CategoryResult> content = categoryRepository.findAll(request.page(), request.size())
                .stream()
                .map(CategoryResult::from)
                .toList();

        long total = categoryRepository.count();
        return CategoryPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public CategoryResult activateCategory(UUID categoryId) {
        Category category = findCategory(categoryId);
        category.activate();
        return CategoryResult.from(categoryRepository.save(category));
    }

    @Override
    public CategoryResult deactivateCategory(UUID categoryId) {
        Category category = findCategory(categoryId);
        category.deactivate();
        return CategoryResult.from(categoryRepository.save(category));
    }

    // ---- helpers ----

    private Category findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}