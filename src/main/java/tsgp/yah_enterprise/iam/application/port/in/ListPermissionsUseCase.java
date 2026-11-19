package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.PermissionPageRequest;
import tsgp.yah_enterprise.iam.application.dto.PermissionPageResult;

public interface ListPermissionsUseCase {

    /**
     * Returns one page of permissions.
     */
    PermissionPageResult listPermissions(PermissionPageRequest request);
}