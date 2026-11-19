package tsgp.yah_enterprise.iam.application.port.out;

import tsgp.yah_enterprise.iam.domain.valueobject.PasswordHash;
import tsgp.yah_enterprise.iam.domain.valueobject.RawPassword;

/**
 * Outbound port for hashing and checking passwords. The implementation lives in the infrastructure layer.
 */
public interface PasswordHasher {

    /**
     * Hashes a password that has already passed the password rules.
     */
    PasswordHash hash(RawPassword rawPassword);

    /**
     * Checks a typed password against a stored hash.
     * Takes a plain String on purpose: at login the password must be checked as typed,
     * even if it would not satisfy today's password rules.
     */
    boolean matches(String rawPassword, PasswordHash passwordHash);

    /**
     * Spends about the same time as {@link #matches}, without checking anything.
     * Used when a login names a user that does not exist, so that response times do not reveal
     * which usernames exist.
     */
    void simulateCheck(String rawPassword);
}