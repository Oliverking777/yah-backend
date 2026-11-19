package tsgp.yah_enterprise.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.iam.domain.valueobject.PasswordHash;

/**
 * Domain model holding the login secret of one user.
 * It refers to its user by userId; the username itself lives only in User,
 * so there is a single source of truth when a username changes.
 * The domain only carries an already-hashed password; hashing and checking are done by the application layer.
 * Identity is defined by credentialId only.
 */
@Getter
public class Credential {

    private final UUID credentialId;
    private final UUID userId;
    private PasswordHash passwordHash;
    private final Instant createdAt;
    private Instant passwordChangedAt;

    private Credential(UUID credentialId, UUID userId, PasswordHash passwordHash,
                       Instant createdAt, Instant passwordChangedAt) {
        this.credentialId = Objects.requireNonNull(credentialId, "credentialId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.passwordChangedAt = Objects.requireNonNull(passwordChangedAt, "passwordChangedAt must not be null");
    }

    /**
     * Creates a brand new credential for a user with a freshly generated id.
     */
    public static Credential createCredential(UUID userId, PasswordHash passwordHash) {
        Instant now = Instant.now();
        return new Credential(UUID.randomUUID(), userId, passwordHash, now, now);
    }

    /**
     * Rebuilds an existing credential from stored data (e.g. from the database).
     */
    public static Credential reconstructCredential(UUID credentialId, UUID userId, PasswordHash passwordHash,
                                                   Instant createdAt, Instant passwordChangedAt) {
        return new Credential(credentialId, userId, passwordHash, createdAt, passwordChangedAt);
    }

    public void changePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "passwordHash must not be null");
        this.passwordChangedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credential other)) return false;
        return credentialId.equals(other.credentialId);
    }

    @Override
    public int hashCode() {
        return credentialId.hashCode();
    }

    @Override
    public String toString() {
        return "Credential{credentialId=" + credentialId + ", userId=" + userId + "}";
    }
}