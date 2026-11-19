package tsgp.yah_enterprise.customer.application.port.in;

import tsgp.yah_enterprise.customer.application.dto.CustomerResult;
import tsgp.yah_enterprise.customer.application.dto.UpdateCustomerCommand;

import java.util.UUID;

public interface UpdateCustomerUseCase {
    CustomerResult updateCustomer(UUID customerId, UpdateCustomerCommand command);
}
