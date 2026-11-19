package tsgp.yah_enterprise.customer.domain.exception;

import java.util.UUID;

public class CustomerAlreadyInactiveException extends CustomerDomainException {
    public CustomerAlreadyInactiveException(UUID customerId) {
        super("Customer is already inactive: " + customerId);
    }
}
