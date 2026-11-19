package tsgp.yah_enterprise.iam.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.PermissionJpaEntity;

/**
 * Converts between the domain Permission and the PermissionJpaEntity.
 */
@Component
public class PermissionMapper {

    public PermissionJpaEntity toEntity(Permission permission) {
        return new PermissionJpaEntity(
                permission.getPermissionId(),
                permission.getPermissionCode().value(),
                permission.getCreatedAt());
    }

    public Permission toDomain(PermissionJpaEntity entity) {
        return Permission.reconstructPermission(
                entity.getPermissionId(),
                entity.getPermissionCode(),
                entity.getCreatedAt());
    }
}