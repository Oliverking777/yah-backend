package tsgp.yah_enterprise.iam.domain.valueobject;

import tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException;

/**
 * A password that has already been hashed (e.g. by bcrypt in the application layer).
 * The domain never hashes or verifies passwords itself; it only carries the hash.
 * toString() never reveals the value.
 */
public record PasswordHash(String value) {

    private static final int MAX_LENGTH = 255;

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException("The password hash must not be blank.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidPasswordException("The password hash must not exceed " + MAX_LENGTH + " characters.");
        }
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    @Override
    public String toString() {
        return "PasswordHash[protected]";
    }
}