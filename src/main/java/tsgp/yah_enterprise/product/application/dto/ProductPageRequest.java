package tsgp.yah_enterprise.product.application.dto;

import tsgp.yah_enterprise.product.domain.model.ProductStatus;

public record ProductPageRequest(int page, int size, String search, ProductStatus status) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public ProductPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
