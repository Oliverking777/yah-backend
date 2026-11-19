package tsgp.yah_enterprise.customer.domain.valueobject;

import tsgp.yah_enterprise.customer.domain.exception.InvalidCustomerException;

public record CustomerCode(String value) {

    public CustomerCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerException("Customer code is required");
        }
    }

    public static CustomerCode of(String value) {
        return new CustomerCode(value);
    }

    /** Formats a sequence number into CUS-000001 style. */
    public static CustomerCode fromSequence(long seq) {
        return new CustomerCode("CUS-" + String.format("%06d", seq));
    }

    @Override
    public String toString() {
        return value;
    }
}
