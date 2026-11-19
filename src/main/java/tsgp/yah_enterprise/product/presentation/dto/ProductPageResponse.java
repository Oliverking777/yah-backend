package tsgp.yah_enterprise.product.presentation.dto;

import tsgp.yah_enterprise.product.application.dto.ProductPageResult;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> content, int page, int size,
        long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {

    public static ProductPageResponse from(ProductPageResult r) {
        return new ProductPageResponse(
                r.content().stream().map(ProductResponse::from).toList(),
                r.page(), r.size(), r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
