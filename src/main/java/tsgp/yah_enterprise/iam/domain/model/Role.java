package tsgp.yah_enterprise.iam.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.iam.domain.exception.InvalidRoleDescriptionException;
import tsgp.yah_enterprise.iam.domain.exception.SystemRoleProtectedException;
import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

@Getter
public class Role {

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final UUID roleId;
    private RoleName roleName;
    private String description;
    /** Whether users of this role must be assigned to a location (ASSIGNED) or work everywhere (ALL). */
    private LocationScope locationScope;
    /** Built-in roles (e.g. the owner) cannot be renamed, deleted, or have their scope changed. */
    private final boolean system;
    private final Instant createdAt;
    private Instant updatedAt;
    /** Permissions are referenced by id only, so Role and Permission stay independent. */
    private final Set<UUID> permissionIds;

    private Role(UUID roleId, RoleName roleName, String description, LocationScope locationScope, boolean system,
                 Instant createdAt, Instant updatedAt, Set<UUID> permissionIds) {
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.roleName = Objects.requireNonNull(roleName, "roleName must not be null");
        this.description = validateDescription(description);
        this.locationScope = Objects.requireNonNull(locationScope, "locationScope must not be null");
        this.system = system;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.permissionIds = copyOf(permissionIds);
    }

    /**
     * Creates a brand new custom role (with no permissions) that requires a location, with a freshly generated id.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException        if the name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleDescriptionException if the description is invalid
     */
    public static Role createRole(String roleName, String description) {
        return createRole(roleName, description, LocationScope.ASSIGNED);
    }

    /**
     * Creates a brand new custom role (with no permissions) and a freshly generated id.
     */
    public static Role createRole(String roleName, String description, LocationScope locationScope) {
        Instant now = Instant.now();
        return new Role(UUID.randomUUID(), RoleName.of(roleName), description, locationScope, false,
                now, now, Set.of());
    }

    /**
     * Creates a built-in role (e.g. the owner). Meant for the application's initial data, not for the API.
     */
    public static Role createSystemRole(String roleName, String description, LocationScope locationScope) {
        Instant now = Instant.now();
        return new Role(UUID.randomUUID(), RoleName.of(roleName), description, locationScope, true,
                now, now, Set.of());
    }

    /**
     * Rebuilds an existing role from stored data (e.g. from the database).
     */
    public static Role reconstructRole(UUID roleId, String roleName, String description,
                                       LocationScope locationScope, boolean system,
                                       Instant createdAt, Instant updatedAt, Set<UUID> permissionIds) {
        return new Role(roleId, RoleName.of(roleName), description, locationScope, system,
                createdAt, updatedAt, permissionIds);
    }

    /**
     * @throws SystemRoleProtectedException if this is a system role
     */
    public void changeRoleName(String newRoleName) {
        requireNotSystem("be renamed");
        this.roleName = RoleName.of(newRoleName);
        touch();
    }

    /**
     * @throws SystemRoleProtectedException if this is a system role
     */
    public void changeLocationScope(LocationScope newLocationScope) {
        Objects.requireNonNull(newLocationScope, "locationScope must not be null");
        if (this.locationScope == newLocationScope) {
            return;
        }
        requireNotSystem("change its location scope");
        this.locationScope = newLocationScope;
        touch();
    }

    /** True if users of this role must be assigned to a location. */
    public boolean requiresLocation() {
        return locationScope.requiresLocation();
    }

    /**
     * Call before deleting the role.
     *
     * @throws SystemRoleProtectedException if this is a system role
     */
    public void ensureDeletable() {
        requireNotSystem("be deleted");
    }

    public void changeDescription(String newDescription) {
        this.description = validateDescription(newDescription);
        touch();
    }

    // ---------- permissions ----------

    /** Read-only view of the ids of the permissions granted by this role. */
    public Set<UUID> getPermissionIds() {
        return Collections.unmodifiableSet(permissionIds);
    }

    public boolean hasPermission(UUID permissionId) {
        return permissionIds.contains(permissionId);
    }

    public void assignPermission(UUID permissionId) {
        Objects.requireNonNull(permissionId, "permissionId must not be null");
        if (permissionIds.add(permissionId)) {
            touch();
        }
    }

    public void removePermission(UUID permissionId) {
        if (permissionIds.remove(permissionId)) {
            touch();
        }
    }

    /**
     * Adds several permissions at once. Ids the role already has are ignored.
     */
    public void assignPermissions(Set<UUID> newPermissionIds) {
        Set<UUID> copy = copyOf(newPermissionIds);
        if (permissionIds.addAll(copy)) {
            touch();
        }
    }

    /**
     * Removes several permissions at once. Ids the role does not have are ignored.
     */
    public void removePermissions(Set<UUID> permissionIdsToRemove) {
        Set<UUID> copy = copyOf(permissionIdsToRemove);
        if (permissionIds.removeAll(copy)) {
            touch();
        }
    }

    /**
     * Replaces the whole set of permissions. An empty set removes all of them.
     */
    public void replacePermissions(Set<UUID> newPermissionIds) {
        Set<UUID> copy = copyOf(newPermissionIds);
        if (!permissionIds.equals(copy)) {
            permissionIds.clear();
            permissionIds.addAll(copy);
            touch();
        }
    }

    // ---------- helpers ----------

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private void requireNotSystem(String action) {
        if (system) {
            throw new SystemRoleProtectedException(roleName.value(), action);
        }
    }

    private static Set<UUID> copyOf(Set<UUID> ids) {
        Objects.requireNonNull(ids, "permissionIds must not be null");
        ids.forEach(id -> Objects.requireNonNull(id, "permissionIds must not contain null"));
        return new HashSet<>(ids);
    }

    private static String validateDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidRoleDescriptionException(
                    "It must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return roleId.equals(other.roleId);
    }

    @Override
    public int hashCode() {
        return roleId.hashCode();
    }

    @Override
    public String toString() {
        return "Role{roleId=" + roleId + ", roleName=" + roleName + "}";
    }
}