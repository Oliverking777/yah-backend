package tsgp.yah_enterprise.customer.application.port.in;

import tsgp.yah_enterprise.customer.application.dto.CreateCustomerCommand;
import tsgp.yah_enterprise.customer.application.dto.CustomerResult;

public interface CreateCustomerUseCase {
    CustomerResult createCustomer(CreateCustomerCommand command);
}
