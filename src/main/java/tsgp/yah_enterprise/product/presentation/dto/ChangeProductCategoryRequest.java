package tsgp.yah_enterprise.product.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChangeProductCategoryRequest(@NotNull(message = "Category id is required") UUID categoryId) {}
