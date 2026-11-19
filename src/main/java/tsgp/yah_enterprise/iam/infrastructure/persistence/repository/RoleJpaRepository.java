package tsgp.yah_enterprise.iam.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.RoleJpaEntity;

/**
 * Spring Data repository. Used only by the adapter, never by the domain.
 */
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    Optional<RoleJpaEntity> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    @Query("select count(r) > 0 from RoleJpaEntity r join r.permissionIds p where p = :permissionId")
    boolean existsByPermissionId(@Param("permissionId") UUID permissionId);
}