package tsgp.yah_enterprise.iam.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

/**
 * @author  mr-oliver
 * @version 1.2
 * Repository interface (port) for managing roles in the system.
 * The implementation lives in the infrastructure layer.
 */
public interface RoleRepository {

    /**
     * Saves a new role or updates an existing one.
     */
    Role save(Role role);

    Optional<Role> findById(UUID roleId);

    Optional<Role> findByName(RoleName roleName);

    /**
     * Returns every role, ordered by name.
     */
    List<Role> findAll();

    /**
     * Returns one page of roles, ordered by name.
     *
     * @param page zero-based page index
     * @param size number of roles per page
     */
    List<Role> findAll(int page, int size);

    /**
     * Returns the total number of roles.
     */
    long count();

    boolean existsById(UUID roleId);

    boolean existsByName(RoleName roleName);

    /**
     * Returns true if at least one role grants the given permission.
     */
    boolean existsByPermissionId(UUID permissionId);

    void deleteById(UUID roleId);
}