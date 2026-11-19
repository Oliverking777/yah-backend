package tsgp.yah_enterprise.iam.domain.valueobject;

import java.util.regex.Pattern;

import tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException;

/**
 * Value object for a role name.
 * Rules: 3 to 50 characters, letters, digits and underscores only.
 * The value is trimmed and stored in upper case (e.g. "admin " -> "ADMIN").
 */
public record RoleName(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    public RoleName {
        if (value == null || value.isBlank()) {
            throw new InvalidRoleNameException(value, "It must not be blank.");
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new InvalidRoleNameException(value,
                    "It must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new InvalidRoleNameException(value,
                    "Only letters, digits and underscores are allowed.");
        }

        value = normalized;
    }

    public static RoleName of(String value) {
        return new RoleName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}