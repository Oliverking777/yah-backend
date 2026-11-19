package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.CreatePermissionCommand;
import tsgp.yah_enterprise.iam.application.dto.PermissionResult;

public interface CreatePermissionUseCase {

    /**
     * Creates a new permission.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException  if the code is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionAlreadyExistsException if the code is already taken
     */
    PermissionResult createPermission(CreatePermissionCommand command);
}