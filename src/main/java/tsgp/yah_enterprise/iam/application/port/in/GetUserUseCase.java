package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.UserResult;

public interface GetUserUseCase {

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException if no user has this id
     */
    UserResult getUserById(UUID userId);

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidUsernameException if the username is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException    if no user has this username
     */
    UserResult getUserByUserName(String userName);
}