package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.PermissionResult;

public interface GetPermissionUseCase {

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException if no permission has this id
     */
    PermissionResult getPermissionById(UUID permissionId);

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException if the code is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException    if no permission has this code
     */
    PermissionResult getPermissionByCode(String permissionCode);
}