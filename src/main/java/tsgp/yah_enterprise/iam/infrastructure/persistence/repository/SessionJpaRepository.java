package tsgp.yah_enterprise.iam.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.SessionJpaEntity;

/**
 * Spring Data repository. Used only by the adapter, never by the domain.
 * <p>
 * The delete methods are explicit bulk queries on purpose: they run immediately.
 * A derived deleteBy... method would only be flushed at the end of the transaction, AFTER the insert of the
 * new session, and would break the one-session-per-user unique constraint at login.
 */
public interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, UUID> {

    Optional<SessionJpaEntity> findByTokenHash(String tokenHash);

    Optional<SessionJpaEntity> findByUserId(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SessionJpaEntity s where s.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from SessionJpaEntity s where s.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}