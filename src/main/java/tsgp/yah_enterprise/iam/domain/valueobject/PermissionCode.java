package tsgp.yah_enterprise.iam.domain.valueobject;

import java.util.regex.Pattern;

import tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException;

/**
 * Value object for a permission code, e.g. ROLE_CREATE or USER_READ.
 * Rules: 3 to 100 characters, letters, digits and underscores only.
 * It must start with a letter, and must not start or end with an underscore
 * or contain two underscores in a row.
 * The value is trimmed and stored in upper case (e.g. " role_create " -> "ROLE_CREATE").
 */
public record PermissionCode(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");

    public PermissionCode {
        if (value == null || value.isBlank()) {
            throw new InvalidPermissionCodeException(value, "It must not be blank.");
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new InvalidPermissionCodeException(value,
                    "It must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new InvalidPermissionCodeException(value,
                    "Use letters, digits and single underscores only, starting with a letter (e.g. ROLE_CREATE).");
        }

        value = normalized;
    }

    public static PermissionCode of(String value) {
        return new PermissionCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}