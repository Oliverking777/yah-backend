package tsgp.yah_enterprise.iam.domain.valueobject;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

import tsgp.yah_enterprise.iam.domain.exception.InvalidSessionTokenException;

/**
 * The opaque session token given to the client: 32 random bytes (256 bits), URL-safe Base64, 43 characters.
 * It carries no information and cannot be guessed. It is shown to the client once, at login,
 * and is never stored: only its SHA-256 hash is (see SessionTokenHash).
 * toString() never reveals it.
 */
public record SessionToken(String value) {

    private static final int RANDOM_BYTES = 32;
    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9_-]{43}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    public SessionToken {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidSessionTokenException();
        }
    }

    /**
     * Creates a brand new random token.
     */
    public static SessionToken generate() {
        byte[] bytes = new byte[RANDOM_BYTES];
        RANDOM.nextBytes(bytes);
        return new SessionToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    /**
     * Wraps a token received from a client.
     *
     * @throws InvalidSessionTokenException if it does not have the format of a token we issue
     */
    public static SessionToken of(String value) {
        return new SessionToken(value);
    }

    @Override
    public String toString() {
        return "SessionToken[protected]";
    }
}