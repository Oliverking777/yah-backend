package tsgp.yah_enterprise.supplier.application.dto;

import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;

public record SupplierPageRequest(int page, int size, String search, SupplierStatus status) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public SupplierPageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}
