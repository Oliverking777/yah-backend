package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.UserPageRequest;
import tsgp.yah_enterprise.iam.application.dto.UserPageResult;

public interface ListUsersUseCase {

    /**
     * Returns one page of users, ordered by username.
     */
    UserPageResult listUsers(UserPageRequest request);
}