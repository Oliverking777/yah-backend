package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.*;
import java.util.UUID;

public interface UpdateProductUseCase { ProductResult updateProduct(UUID productId, UpdateProductCommand command); }
