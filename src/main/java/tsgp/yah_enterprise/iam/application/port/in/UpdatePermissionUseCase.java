package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.PermissionResult;
import tsgp.yah_enterprise.iam.application.dto.UpdatePermissionCommand;

public interface UpdatePermissionUseCase {

    /**
     * Changes the code of an existing permission.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException      if no permission has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException   if the new code is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionAlreadyExistsException if the new code belongs to another permission
     */
    PermissionResult updatePermission(UUID permissionId, UpdatePermissionCommand command);
}