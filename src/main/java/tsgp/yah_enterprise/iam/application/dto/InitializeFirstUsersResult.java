package tsgp.yah_enterprise.iam.application.dto;

/**
 * What the initialization did.
 *
 * @param usersAlreadyExist true if the system already had users, so nothing was done
 * @param admin             the first admin, or null if none was created
 * @param owner             the first owner, or null if none was created
 */
public record InitializeFirstUsersResult(
        boolean usersAlreadyExist,
        UserResult admin,
        UserResult owner
) {
    public boolean nothingConfigured() {
        return !usersAlreadyExist && admin == null && owner == null;
    }
}