package tsgp.yah_enterprise.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.application.port.out.PasswordHasher;
import tsgp.yah_enterprise.iam.domain.valueobject.PasswordHash;
import tsgp.yah_enterprise.iam.domain.valueobject.RawPassword;

/**
 * bcrypt implementation of the PasswordHasher port (uses spring-security-crypto).
 */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private static final int MAX_BYTES = 72; // bcrypt ignores or rejects anything longer

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** A real bcrypt hash of a throw-away value, used only to burn the same time as a real check. */
    private final String dummyHash = encoder.encode("not-a-real-password-1");

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        return PasswordHash.of(encoder.encode(rawPassword.value()));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash) {
        if (rawPassword == null || tooLong(rawPassword)) {
            return false; // a password this long can never have been accepted
        }
        return encoder.matches(rawPassword, passwordHash.value());
    }

    @Override
    public void simulateCheck(String rawPassword) {
        if (rawPassword == null || tooLong(rawPassword)) {
            return; // matches() also returns immediately for these, so the timing stays equal
        }
        encoder.matches(rawPassword, dummyHash);
    }

    private static boolean tooLong(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}