package tsgp.yah_enterprise.iam.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;

/**
 * Repository interface (port) for managing permissions in the system.
 * The implementation lives in the infrastructure layer.
 */
public interface PermissionRepository {

    /**
     * Saves a new permission or updates an existing one.
     */
    Permission save(Permission permission);

    Optional<Permission> findById(UUID permissionId);

    Optional<Permission> findByCode(PermissionCode permissionCode);

    /**
     * Returns every permission, ordered by code.
     */
    List<Permission> findAll();

    /**
     * Returns one page of permissions, ordered by code.
     *
     * @param page zero-based page index
     * @param size number of permissions per page
     */
    List<Permission> findAll(int page, int size);

    /**
     * Returns the total number of permissions.
     */
    long count();

    /**
     * Returns the permissions that exist for the given ids. Unknown ids are simply missing from the result.
     */
    List<Permission> findAllByIds(Collection<UUID> permissionIds);

    boolean existsById(UUID permissionId);

    boolean existsByCode(PermissionCode permissionCode);

    void deleteById(UUID permissionId);
}