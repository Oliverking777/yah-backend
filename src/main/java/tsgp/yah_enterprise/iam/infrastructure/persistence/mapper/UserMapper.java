package tsgp.yah_enterprise.iam.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.domain.model.User;
import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.UserJpaEntity;

/**
 * Converts between the domain User and the UserJpaEntity.
 * The stored full_name is only a copy for queries; the domain always recomputes it from first and last name.
 */
@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getUserName().value(),
                user.getRoleId(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastActiveAt());
    }

    public User toDomain(UserJpaEntity entity) {
        return User.reconstructUser(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getUserName(),
                entity.getRoleId(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastActiveAt());
    }
}