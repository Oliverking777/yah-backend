package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {

    /**
     * Deletes a user together with their credential.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException if no user has this id
     */
    void deleteUser(UUID userId);
}