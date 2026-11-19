package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the InitializeFirstUsersUseCase: the details of the first ADMIN and OWNER users.
 * A password that is null or blank means: do not create that user.
 */
public record InitializeFirstUsersCommand(
        String adminFirstName,
        String adminLastName,
        String adminPassword,
        String ownerFirstName,
        String ownerLastName,
        String ownerPassword
) {
    @Override
    public String toString() {
        return "InitializeFirstUsersCommand[adminFirstName=" + adminFirstName + ", adminLastName=" + adminLastName
                + ", ownerFirstName=" + ownerFirstName + ", ownerLastName=" + ownerLastName
                + ", passwords=protected]";
    }
}