package tsgp.yah_enterprise.location.infrastructure.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.RoleResult;
import tsgp.yah_enterprise.iam.application.dto.UserResult;
import tsgp.yah_enterprise.iam.application.port.in.GetRoleUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetUserUseCase;
import tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException;
import tsgp.yah_enterprise.location.domain.model.LocationUser;
import tsgp.yah_enterprise.location.domain.port.UserDirectoryPort;

/**
 * Implements Location's UserDirectoryPort on top of IAM's inbound ports.
 * This is the only place in the Location module that knows IAM exists:
 * it uses IAM use cases and DTOs, never IAM entities or repositories.
 */
@Component
@RequiredArgsConstructor
public class IamUserDirectoryAdapter implements UserDirectoryPort {

    /** The location scope value of roles whose users work across every location. */
    private static final String SCOPE_ALL = "ALL";

    private final GetUserUseCase getUserUseCase;
    private final GetRoleUseCase getRoleUseCase;

    /**
     * What Location needs to know about a user's role: a display name, and whether the role needs a location.
     * Location never looks at the role's name to decide this; it only reads the role's own scope.
     */
    private record RoleInfo(String name, boolean locationRequired) {
    }

    @Override
    public Optional<LocationUser> findById(UUID userId) {
        return fetchUser(userId).map(user -> toLocationUser(user, resolveRole(user.roleId())));
    }

    @Override
    public List<LocationUser> findAllByIds(Collection<UUID> userIds) {
        // Roles are looked up once per distinct role, not once per user.
        Map<UUID, RoleInfo> roles = new HashMap<>();

        return new LinkedHashSet<>(userIds).stream()
                .map(this::fetchUser)
                .flatMap(Optional::stream)
                .map(user -> toLocationUser(
                        user, roles.computeIfAbsent(user.roleId(), this::resolveRole)))
                .toList();
    }

    // ---- helpers ----

    private Optional<UserResult> fetchUser(UUID userId) {
        try {
            return Optional.of(getUserUseCase.getUserById(userId));
        } catch (UserNotFoundException e) {
            return Optional.empty();
        }
    }

    private RoleInfo resolveRole(UUID roleId) {
        try {
            RoleResult role = getRoleUseCase.getRoleById(roleId);
            return new RoleInfo(String.valueOf(role.roleName()), !SCOPE_ALL.equals(role.locationScope()));
        } catch (RoleNotFoundException e) {
            // Cannot happen while users.role_id has a foreign key to roles. If it ever does, the name is
            // unknown and the role is treated like the default scope (a location is required).
            return new RoleInfo(null, true);
        }
    }

    private LocationUser toLocationUser(UserResult user, RoleInfo role) {
        return new LocationUser(
                user.userId(),
                user.fullName(),
                String.valueOf(user.userName()),
                role.name(),
                user.active(),
                role.locationRequired());
    }
}