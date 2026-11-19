package tsgp.yah_enterprise.payment.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.customer.application.dto.CustomerResult;
import tsgp.yah_enterprise.customer.application.port.in.GetCustomerUseCase;
import tsgp.yah_enterprise.customer.domain.exception.CustomerNotFoundException;
import tsgp.yah_enterprise.payment.domain.port.PaymentCustomerDirectoryPort;

import java.util.Optional;
import java.util.UUID;

/** Payment reads the Customer module through its public use case only. */
@Component
@RequiredArgsConstructor
public class PaymentCustomerDirectoryAdapter implements PaymentCustomerDirectoryPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetCustomerUseCase getCustomerUseCase;

    @Override
    public Optional<PaymentCustomer> findCustomer(UUID customerId) {
        try {
            CustomerResult c = getCustomerUseCase.getCustomer(customerId);
            return Optional.of(new PaymentCustomer(c.customerId(), c.customerName(),
                    ACTIVE.equals(c.customerStatus()), c.walkIn()));
        } catch (CustomerNotFoundException e) {
            return Optional.empty();
        }
    }
}
