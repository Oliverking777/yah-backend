package tsgp.yah_enterprise.customer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.customer.application.dto.CustomerResult;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A customer")
public record CustomerResponse(
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
    public static CustomerResponse from(CustomerResult r) {
        return new CustomerResponse(r.customerId(), r.customerCode(), r.customerName(),
                r.phoneNumber(), r.email(), r.address(), r.customerType(), r.customerStatus(),
                r.walkIn(), r.createdAt(), r.updatedAt());
    }
}
