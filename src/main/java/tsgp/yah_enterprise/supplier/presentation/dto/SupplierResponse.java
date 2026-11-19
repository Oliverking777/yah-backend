package tsgp.yah_enterprise.supplier.presentation.dto;

import tsgp.yah_enterprise.supplier.application.dto.SupplierResult;
import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
        UUID supplierId, String supplierCode, String supplierName,
        String contactPerson, String phoneNumber, String email,
        String address, String supplierStatus,
        Instant createdAt, Instant updatedAt) {

    public static SupplierResponse from(SupplierResult r) {
        return new SupplierResponse(r.supplierId(), r.supplierCode(), r.supplierName(),
                r.contactPerson(), r.phoneNumber(), r.email(), r.address(),
                r.supplierStatus(), r.createdAt(), r.updatedAt());
    }
}
