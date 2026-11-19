package tsgp.yah_enterprise.iam.domain.valueobject;

import java.util.regex.Pattern;

import tsgp.yah_enterprise.iam.domain.exception.InvalidUsernameException;

/**
 * Value object for a login name.
 * Rules: 3 to 50 characters; lower-case letters, digits, dots, underscores and hyphens;
 * it must start and end with a letter or digit.
 * The value is trimmed and stored in lower case (e.g. " John.Doe " -> "john.doe"),
 * so usernames are case-insensitive.
 */
public record Username(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9._-]*[a-z0-9])?$");

    public Username {
        if (value == null || value.isBlank()) {
            throw new InvalidUsernameException(value, "It must not be blank.");
        }

        String normalized = value.trim().toLowerCase();

        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new InvalidUsernameException(value,
                    "It must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new InvalidUsernameException(value,
                    "Use letters, digits, dots, underscores and hyphens only, starting and ending with a letter or digit.");
        }

        value = normalized;
    }

    public static Username of(String value) {
        return new Username(value);
    }

    @Override
    public String toString() {
        return value;
    }
}