package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.ChangeOwnPasswordCommand;

public interface ChangeOwnPasswordUseCase {

    /**
     * Lets a logged-in user change their own password. The current password must be given and be correct.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.IncorrectCurrentPasswordException if the current password is wrong
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException           if the new password breaks the
     *         rules, or is the same as the current one
     * @throws tsgp.yah_enterprise.iam.domain.exception.CredentialNotFoundException       if the user has no credential
     */
    void changeOwnPassword(UUID userId, ChangeOwnPasswordCommand command);
}