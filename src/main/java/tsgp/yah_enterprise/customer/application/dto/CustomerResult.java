package tsgp.yah_enterprise.customer.application.dto;

import tsgp.yah_enterprise.customer.domain.model.Customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResult(
        UUID customerId,
        String customerCode,
        String customerName,
        String phoneNumber,
        String email,
        String address,
        String customerType,
        String customerStatus,
        boolean walkIn,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResult from(Customer c) {
        return new CustomerResult(
                c.getCustomerId(),
                c.getCustomerCode().value(),
                c.getCustomerName().value(),
                c.getPhoneNumber() != null ? c.getPhoneNumber().value() : null,
                c.getEmail() != null ? c.getEmail().value() : null,
                c.getAddress(),
                c.getCustomerType().name(),
                c.getCustomerStatus().name(),
                c.isWalkIn(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }
}
