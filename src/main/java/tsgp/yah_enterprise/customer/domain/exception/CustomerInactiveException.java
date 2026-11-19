package tsgp.yah_enterprise.customer.domain.exception;

import java.util.UUID;

public class CustomerInactiveException extends CustomerDomainException {
    public CustomerInactiveException(UUID customerId) {
        super("Customer is inactive: " + customerId);
    }
}
