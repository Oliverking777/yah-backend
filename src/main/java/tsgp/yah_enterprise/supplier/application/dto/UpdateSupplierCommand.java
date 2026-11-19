package tsgp.yah_enterprise.supplier.application.dto;

public record UpdateSupplierCommand(
        String supplierName, String contactPerson,
        String phoneNumber, String email, String address) {}
