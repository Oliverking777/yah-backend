package tsgp.yah_enterprise.iam.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.LocationScope;

/**
 * Who is making a request, as established from a valid session.
 * permissions are the permission codes (e.g. ROLE_CREATE) granted by the user's role,
 * looked up fresh on every request, so changes to a role take effect immediately.
 * locationScope says whether the user must belong to a location (ASSIGNED) or works everywhere (ALL).
 */
public record AuthenticatedUser(
        UUID userId,
        String userName,
        String fullName,
        UUID roleId,
        String roleName,
        LocationScope locationScope,
        Set<String> permissions,
        UUID sessionId,
        Instant sessionExpiresAt
) {
    public AuthenticatedUser {
        permissions = Set.copyOf(permissions);
    }

    /** True if this user must be assigned to a location. */
    public boolean requiresLocation() {
        return locationScope.requiresLocation();
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.contains(permissionCode);
    }
}