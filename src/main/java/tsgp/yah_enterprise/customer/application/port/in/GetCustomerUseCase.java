package tsgp.yah_enterprise.customer.application.port.in;

import tsgp.yah_enterprise.customer.application.dto.CustomerResult;

import java.util.UUID;

public interface GetCustomerUseCase {
    CustomerResult getCustomer(UUID customerId);
}
