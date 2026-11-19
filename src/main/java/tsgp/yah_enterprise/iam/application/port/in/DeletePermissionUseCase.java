package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

public interface DeletePermissionUseCase {

    /**
     * Deletes a permission.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException if no permission has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionInUseException    if a role still grants this permission
     */
    void deletePermission(UUID permissionId);
}