package tsgp.yah_enterprise.iam.infrastructure.persistence.mapper;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.RoleJpaEntity;

/**
 * Converts between the domain Role and the RoleJpaEntity.
 */
@Component
public class RoleMapper {

    public RoleJpaEntity toEntity(Role role) {
        return new RoleJpaEntity(
                role.getRoleId(),
                role.getRoleName().value(),
                role.getDescription(),
                role.getLocationScope(),
                role.isSystem(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                new HashSet<>(role.getPermissionIds()));
    }

    public Role toDomain(RoleJpaEntity entity) {
        return Role.reconstructRole(
                entity.getRoleId(),
                entity.getRoleName(),
                entity.getDescription(),
                entity.getLocationScope(),
                entity.isSystem(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPermissionIds());
    }
}