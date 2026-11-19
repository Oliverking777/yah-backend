package tsgp.yah_enterprise.payment.domain.port;

import java.util.Optional;
import java.util.UUID;

/** Payment's view of the Customer module. Implemented in infrastructure/integration. */
public interface PaymentCustomerDirectoryPort {

    Optional<PaymentCustomer> findCustomer(UUID customerId);

    record PaymentCustomer(UUID customerId, String customerName, boolean active, boolean walkIn) {
    }
}
