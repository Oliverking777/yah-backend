package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the ChangeOwnPasswordUseCase.
 */
public record ChangeOwnPasswordCommand(
        String currentPassword,
        String newPassword
) {
    @Override
    public String toString() {
        return "ChangeOwnPasswordCommand[currentPassword=protected, newPassword=protected]";
    }
}