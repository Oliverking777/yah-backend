package tsgp.yah_enterprise.product.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.category.application.port.in.GetCategoryUseCase;
import tsgp.yah_enterprise.category.domain.exception.CategoryNotFoundException;
import tsgp.yah_enterprise.product.domain.port.CategoryDirectoryPort;
import tsgp.yah_enterprise.product.domain.port.ProductCategory;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryDirectoryAdapter implements CategoryDirectoryPort {

    private final GetCategoryUseCase getCategoryUseCase;

    @Override
    public Optional<ProductCategory> findById(UUID categoryId) {
        try {
            var result = getCategoryUseCase.getCategoryById(categoryId);
            return Optional.of(new ProductCategory(
                    result.categoryId(), result.categoryName(),
                    "ACTIVE".equals(result.categoryStatus())));
        } catch (CategoryNotFoundException e) {
            return Optional.empty();
        }
    }
}
