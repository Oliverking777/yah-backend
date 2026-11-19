package tsgp.yah_enterprise.customer.domain.exception;

import java.util.UUID;

public class CustomerNotFoundException extends CustomerDomainException {
    public CustomerNotFoundException(UUID customerId) {
        super("Customer not found: " + customerId);
    }
}
