package tsgp.yah_enterprise.sales.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.customer.application.dto.CustomerResult;
import tsgp.yah_enterprise.customer.application.port.in.GetCustomerUseCase;
import tsgp.yah_enterprise.customer.domain.exception.CustomerNotFoundException;
import tsgp.yah_enterprise.sales.domain.port.SalesCustomerDirectoryPort;

import java.util.Optional;
import java.util.UUID;

/** Sales reads the Customer module through its public use case only. */
@Component
@RequiredArgsConstructor
public class SalesCustomerDirectoryAdapter implements SalesCustomerDirectoryPort {

    private final GetCustomerUseCase getCustomerUseCase;

    @Override
    public Optional<SalesCustomer> findById(UUID customerId) {
        try {
            CustomerResult c = getCustomerUseCase.getCustomer(customerId);
            return Optional.of(new SalesCustomer(c.customerId(), c.customerName(),
                    "ACTIVE".equals(c.customerStatus()), c.walkIn()));
        } catch (CustomerNotFoundException e) {
            return Optional.empty();
        }
    }
}
