package tsgp.yah_enterprise.customer.application.dto;

public record UpdateCustomerCommand(
        String customerName,
        String phoneNumber,
        String email,
        String address
) {}
