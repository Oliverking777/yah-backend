package tsgp.yah_enterprise.supplier.domain.valueobject;

import tsgp.yah_enterprise.supplier.domain.exception.InvalidSupplierException;

public record Email(String value) {
    public static final int MAX_LENGTH = 150;
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public Email {
        if (value == null || value.isBlank()) throw new InvalidSupplierException("Email must not be blank");
        value = value.trim().toLowerCase();
        if (value.length() > MAX_LENGTH) throw new InvalidSupplierException("Email must not exceed " + MAX_LENGTH + " characters");
        if (!value.matches(EMAIL_REGEX)) throw new InvalidSupplierException("Email format is invalid");
    }
    public static Email of(String value) { return new Email(value); }
    @Override public String toString() { return value; }
}
