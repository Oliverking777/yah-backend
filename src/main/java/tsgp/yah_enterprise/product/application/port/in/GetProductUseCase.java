package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.ProductResult;
import java.util.UUID;

public interface GetProductUseCase { ProductResult getProduct(UUID productId); }
