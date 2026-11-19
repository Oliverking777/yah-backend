package tsgp.yah_enterprise.iam.domain.valueobject;

import tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException;

/**
 * A plain-text password as typed by a person. It exists only long enough to be checked and hashed.
 * It is never stored, and toString() never reveals it.
 * Rules: 8 to 72 characters (72 is the limit of bcrypt), with at least one letter and one digit.
 * The value is NOT trimmed: spaces are part of the password.
 */
public record RawPassword(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 72;

    public RawPassword {
        if (value == null || value.isEmpty()) {
            throw new InvalidPasswordException("The password must not be empty.");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidPasswordException(
                    "The password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }
        if (value.chars().noneMatch(Character::isLetter) || value.chars().noneMatch(Character::isDigit)) {
            throw new InvalidPasswordException("The password must contain at least one letter and one digit.");
        }
    }

    public static RawPassword of(String value) {
        return new RawPassword(value);
    }

    @Override
    public String toString() {
        return "RawPassword[protected]";
    }
}