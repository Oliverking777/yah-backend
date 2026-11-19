package tsgp.yah_enterprise.customer.application.port.in;

import tsgp.yah_enterprise.customer.application.dto.CustomerResult;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GetCustomersByIdsUseCase {
    List<CustomerResult> getCustomersByIds(Collection<UUID> customerIds);
}
