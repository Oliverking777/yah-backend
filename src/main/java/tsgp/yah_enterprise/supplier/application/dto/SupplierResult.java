package tsgp.yah_enterprise.supplier.application.dto;

import tsgp.yah_enterprise.supplier.domain.model.Supplier;
import java.time.Instant;
import java.util.UUID;

public record SupplierResult(
        UUID supplierId, String supplierCode, String supplierName,
        String contactPerson, String phoneNumber, String email,
        String address, String supplierStatus,
        Instant createdAt, Instant updatedAt) {

    public static SupplierResult from(Supplier s) {
        return new SupplierResult(
                s.getSupplierId(), s.getSupplierCode().value(), s.getSupplierName().value(),
                s.getContactPerson(),
                s.getPhoneNumber() != null ? s.getPhoneNumber().value() : null,
                s.getEmail() != null ? s.getEmail().value() : null,
                s.getAddress(), s.getSupplierStatus().name(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
