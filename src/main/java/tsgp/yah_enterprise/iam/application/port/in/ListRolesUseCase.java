package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.RolePageRequest;
import tsgp.yah_enterprise.iam.application.dto.RolePageResult;

public interface ListRolesUseCase {

    /**
     * Returns one page of roles.
     */
    RolePageResult listRoles(RolePageRequest request);
}