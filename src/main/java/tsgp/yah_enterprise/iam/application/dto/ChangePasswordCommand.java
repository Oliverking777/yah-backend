package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the ChangeUserPasswordUseCase.
 */
public record ChangePasswordCommand(
        String newPassword
) {
    @Override
    public String toString() {
        return "ChangePasswordCommand[newPassword=protected]";
    }
}