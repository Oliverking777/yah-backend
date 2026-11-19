package tsgp.yah_enterprise.iam.domain.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Session;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionTokenHash;

/**
 * Repository interface (port) for managing sessions.
 * The implementation lives in the infrastructure layer.
 * A user can have only one session at a time.
 */
public interface SessionRepository {

    /**
     * Saves a new session. Any existing session of the same user must be removed first
     * with {@link #deleteByUserId(UUID)}, in the same transaction.
     */
    Session save(Session session);

    Optional<Session> findByTokenHash(SessionTokenHash tokenHash);

    Optional<Session> findByUserId(UUID userId);

    /**
     * Deletes the session of a user, if any. Takes effect immediately.
     */
    void deleteByUserId(UUID userId);

    /**
     * Deletes every session that has expired at the given moment.
     *
     * @return how many sessions were deleted
     */
    int deleteExpired(Instant now);
}