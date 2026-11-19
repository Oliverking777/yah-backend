package tsgp.yah_enterprise.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;

/**
 * Domain model representing a permission in the IAM (Identity and Access Management) context.
 * Identity is defined by permissionId only.
 */
@Getter
public class Permission {

    private final UUID permissionId;
    private PermissionCode permissionCode;
    private final Instant createdAt;

    private Permission(UUID permissionId, PermissionCode permissionCode, Instant createdAt) {
        this.permissionId = Objects.requireNonNull(permissionId, "permissionId must not be null");
        this.permissionCode = Objects.requireNonNull(permissionCode, "permissionCode must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Creates a brand new permission with a freshly generated id.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException if the code is invalid
     */
    public static Permission createPermission(String permissionCode) {
        return new Permission(UUID.randomUUID(), PermissionCode.of(permissionCode), Instant.now());
    }

    /**
     * Rebuilds an existing permission from stored data (e.g. from the database).
     */
    public static Permission reconstructPermission(UUID permissionId, String permissionCode, Instant createdAt) {
        return new Permission(permissionId, PermissionCode.of(permissionCode), createdAt);
    }

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException if the new code is invalid
     */
    public void changePermissionCode(String newPermissionCode) {
        this.permissionCode = PermissionCode.of(newPermissionCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission other)) return false;
        return permissionId.equals(other.permissionId);
    }

    @Override
    public int hashCode() {
        return permissionId.hashCode();
    }

    @Override
    public String toString() {
        return "Permission{permissionId=" + permissionId + ", permissionCode=" + permissionCode + "}";
    }
}