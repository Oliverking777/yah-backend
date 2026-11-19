package tsgp.yah_enterprise.customer.domain.valueobject;

import tsgp.yah_enterprise.customer.domain.exception.InvalidCustomerException;

public record Email(String value) {

    public static final int MAX_LENGTH = 150;
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public Email {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerException("Email must not be blank");
        }
        value = value.trim().toLowerCase();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCustomerException("Email must not exceed " + MAX_LENGTH + " characters");
        }
        if (!value.matches(EMAIL_REGEX)) {
            throw new InvalidCustomerException("Email format is invalid");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
