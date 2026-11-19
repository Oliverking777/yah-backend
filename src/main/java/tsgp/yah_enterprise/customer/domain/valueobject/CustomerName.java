package tsgp.yah_enterprise.customer.domain.valueobject;

import tsgp.yah_enterprise.customer.domain.exception.InvalidCustomerException;

public record CustomerName(String value) {

    public static final int MAX_LENGTH = 150;

    public CustomerName {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerException("Customer name is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCustomerException("Customer name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static CustomerName of(String value) {
        return new CustomerName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
