package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record SupplierProductsRequest(UUID supplierId, int page, int size) {
    public SupplierProductsRequest {
        page = Math.max(page, 0);
        size = size < 1 ? 20 : Math.min(size, 100);
    }
}
