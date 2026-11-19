package tsgp.yah_enterprise.customer.domain.exception;

import java.util.UUID;

public class CustomerAlreadyActiveException extends CustomerDomainException {
    public CustomerAlreadyActiveException(UUID customerId) {
        super("Customer is already active: " + customerId);
    }
}
