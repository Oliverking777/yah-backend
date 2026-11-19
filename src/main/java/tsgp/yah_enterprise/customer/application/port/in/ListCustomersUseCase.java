package tsgp.yah_enterprise.customer.application.port.in;

import tsgp.yah_enterprise.customer.application.dto.CustomerPageRequest;
import tsgp.yah_enterprise.customer.application.dto.CustomerPageResult;

public interface ListCustomersUseCase {
    CustomerPageResult listCustomers(CustomerPageRequest request);
}
