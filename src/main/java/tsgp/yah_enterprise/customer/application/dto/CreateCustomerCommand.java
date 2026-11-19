package tsgp.yah_enterprise.customer.application.dto;

import tsgp.yah_enterprise.customer.domain.model.CustomerType;

public record CreateCustomerCommand(
        String customerName,
        String phoneNumber,
        String email,
        String address,
        CustomerType customerType
) {}
