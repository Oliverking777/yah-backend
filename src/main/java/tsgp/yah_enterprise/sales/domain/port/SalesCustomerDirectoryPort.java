package tsgp.yah_enterprise.sales.domain.port;

import java.util.Optional;
import java.util.UUID;

/** Sales' view of the Customer module. Implemented in infrastructure/integration. */
public interface SalesCustomerDirectoryPort {

    Optional<SalesCustomer> findById(UUID customerId);

    record SalesCustomer(UUID customerId, String customerName, boolean active, boolean walkIn) { }
}
