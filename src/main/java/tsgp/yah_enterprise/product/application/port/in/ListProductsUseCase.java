package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.*;

/** Search matches the product name or code, or the name, SKU or barcode of one of its variants. */
public interface ListProductsUseCase { ProductPageResult listProducts(ProductPageRequest request); }
