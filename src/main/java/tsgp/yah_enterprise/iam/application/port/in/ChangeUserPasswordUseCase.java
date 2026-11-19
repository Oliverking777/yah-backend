package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.ChangePasswordCommand;

public interface ChangeUserPasswordUseCase {

    /**
     * Sets a new password for a user (an administrator reset).
     * A "change my own password" flow that asks for the current password comes with authentication.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException       if no user has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException     if the password breaks the rules
     * @throws tsgp.yah_enterprise.iam.domain.exception.CredentialNotFoundException if the user has no credential
     */
    void changePassword(UUID userId, ChangePasswordCommand command);
}