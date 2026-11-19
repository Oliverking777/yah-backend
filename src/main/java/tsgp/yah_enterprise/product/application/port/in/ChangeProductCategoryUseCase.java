package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.ProductResult;
import java.util.UUID;

public interface ChangeProductCategoryUseCase { ProductResult changeProductCategory(UUID productId, UUID categoryId); }
