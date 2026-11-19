package tsgp.yah_enterprise.iam.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.PermissionJpaEntity;

/**
 * Spring Data repository. Used only by the adapter, never by the domain.
 */
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    Optional<PermissionJpaEntity> findByPermissionCode(String permissionCode);

    boolean existsByPermissionCode(String permissionCode);
}