package tsgp.yah_enterprise.customer.domain.valueobject;

import tsgp.yah_enterprise.customer.domain.exception.InvalidCustomerException;

public record PhoneNumber(String value) {

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidCustomerException("Phone number must not be blank");
        }
        value = value.trim();
        // normalise: keep digits and a leading +
        String digits = value.startsWith("+") ? "+" + value.substring(1).replaceAll("\\D", "")
                                               : value.replaceAll("\\D", "");
        long digitCount = digits.chars().filter(Character::isDigit).count();
        if (digitCount < 8 || digitCount > 15) {
            throw new InvalidCustomerException("Phone number must have 8 to 15 digits");
        }
        value = digits;
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
