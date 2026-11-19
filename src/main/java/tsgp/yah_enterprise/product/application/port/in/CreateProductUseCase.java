package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.*;

/** Creates a product with all its variants and their units at once; the code and SKUs are generated. */
public interface CreateProductUseCase { ProductResult createProduct(CreateProductCommand command); }
