package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.UserResult;

public interface ChangeUserStatusUseCase {

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException if no user has this id
     */
    UserResult activateUser(UUID userId);

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException if no user has this id
     */
    UserResult deactivateUser(UUID userId);
}