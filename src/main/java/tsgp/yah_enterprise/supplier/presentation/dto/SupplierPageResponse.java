package tsgp.yah_enterprise.supplier.presentation.dto;

import tsgp.yah_enterprise.supplier.application.dto.SupplierPageResult;
import java.util.List;

public record SupplierPageResponse(
        List<SupplierResponse> content, int page, int size,
        long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {

    public static SupplierPageResponse from(SupplierPageResult r) {
        return new SupplierPageResponse(
                r.content().stream().map(SupplierResponse::from).toList(),
                r.page(), r.size(), r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
