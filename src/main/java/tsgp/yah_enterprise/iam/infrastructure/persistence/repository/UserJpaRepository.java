package tsgp.yah_enterprise.iam.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.UserJpaEntity;

/**
 * Spring Data repository. Used only by the adapter, never by the domain.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByRoleId(UUID roleId);

    /**
     * Targeted update of last_active_at only. Returns the number of rows changed (0 or 1).
     */
    @Modifying
    @Query("update UserJpaEntity u set u.lastActiveAt = :activeAt "
            + "where u.userId = :userId and (u.lastActiveAt is null or u.lastActiveAt < :staleBefore)")
    int updateLastActiveAt(@Param("userId") UUID userId,
                           @Param("activeAt") Instant activeAt,
                           @Param("staleBefore") Instant staleBefore);
}