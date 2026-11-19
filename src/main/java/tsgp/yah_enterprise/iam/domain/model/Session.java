package tsgp.yah_enterprise.iam.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.iam.domain.exception.SessionExpiredException;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionTokenHash;

/**
 * Domain model representing a login session.
 * <p>
 * Fixed 12-hour lifetime: expiresAt is set once, when the session starts, and never extended,
 * no matter how much the session is used (hard cutoff).
 * A session is immutable. It ends by expiring or by being deleted (logout, or a newer login by the same user).
 * Only the hash of the token is kept, never the token itself.
 * Identity is defined by sessionId only.
 */
@Getter
public class Session {

    /** How long a session lasts from the moment it starts. */
    public static final Duration LIFETIME = Duration.ofHours(12);

    private static final int MAX_IP_LENGTH = 45;          // longest textual IPv6 address
    private static final int MAX_USER_AGENT_LENGTH = 255;

    private final UUID sessionId;
    private final UUID userId;
    private final SessionTokenHash tokenHash;
    private final ClientType clientType;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final String ipAddress;   // for auditing, may be null
    private final String userAgent;   // for auditing, may be null

    private Session(UUID sessionId, UUID userId, SessionTokenHash tokenHash, ClientType clientType,
                    Instant createdAt, Instant expiresAt, String ipAddress, String userAgent) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.clientType = Objects.requireNonNull(clientType, "clientType must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
        this.ipAddress = clean(ipAddress, MAX_IP_LENGTH);
        this.userAgent = clean(userAgent, MAX_USER_AGENT_LENGTH);
    }

    /**
     * Starts a brand new session for a user. It expires {@link #LIFETIME} from now.
     */
    public static Session start(UUID userId, SessionTokenHash tokenHash, ClientType clientType,
                                String ipAddress, String userAgent) {
        Instant now = Instant.now();
        return new Session(UUID.randomUUID(), userId, tokenHash, clientType,
                now, now.plus(LIFETIME), ipAddress, userAgent);
    }

    /**
     * Rebuilds an existing session from stored data (e.g. from the database).
     */
    public static Session reconstructSession(UUID sessionId, UUID userId, SessionTokenHash tokenHash,
                                             ClientType clientType, Instant createdAt, Instant expiresAt,
                                             String ipAddress, String userAgent) {
        return new Session(sessionId, userId, tokenHash, clientType, createdAt, expiresAt, ipAddress, userAgent);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isExpired() {
        return isExpired(Instant.now());
    }

    /**
     * Time left before the session expires (zero if it already has). Useful for a cookie's Max-Age.
     */
    public Duration remainingLifetime(Instant now) {
        return isExpired(now) ? Duration.ZERO : Duration.between(now, expiresAt);
    }

    /**
     * @throws SessionExpiredException if the session has expired
     */
    public void requireNotExpired() {
        if (isExpired()) {
            throw new SessionExpiredException(sessionId);
        }
    }

    private static String clean(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session other)) return false;
        return sessionId.equals(other.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }

    @Override
    public String toString() {
        return "Session{sessionId=" + sessionId + ", userId=" + userId + ", clientType=" + clientType
                + ", expiresAt=" + expiresAt + "}";
    }
}