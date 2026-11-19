package tsgp.yah_enterprise.iam.domain.valueobject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

import tsgp.yah_enterprise.iam.domain.exception.InvalidSessionTokenException;

/**
 * The SHA-256 hash (64 lower-case hex characters) of a SessionToken. This is what is stored in the database,
 * so a leaked database cannot be used to impersonate anyone.
 * A fast hash is fine here (unlike passwords) because the token is 256 bits of randomness, not a human secret.
 */
public record SessionTokenHash(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[0-9a-f]{64}$");

    public SessionTokenHash {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidSessionTokenException("The session token hash is malformed.");
        }
    }

    public static SessionTokenHash from(SessionToken token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.value().getBytes(StandardCharsets.UTF_8));
            return new SessionTokenHash(HexFormat.of().formatHex(hashed));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static SessionTokenHash of(String value) {
        return new SessionTokenHash(value);
    }

    @Override
    public String toString() {
        return "SessionTokenHash[protected]";
    }
}