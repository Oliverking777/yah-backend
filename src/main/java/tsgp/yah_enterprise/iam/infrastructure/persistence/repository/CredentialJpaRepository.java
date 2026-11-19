package tsgp.yah_enterprise.iam.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.CredentialJpaEntity;

/**
 * Spring Data repository. Used only by the adapter, never by the domain.
 */
public interface CredentialJpaRepository extends JpaRepository<CredentialJpaEntity, UUID> {

    Optional<CredentialJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("delete from CredentialJpaEntity c where c.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}