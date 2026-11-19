package tsgp.yah_enterprise.iam.application.dto;

import java.util.UUID;

/**
 * Input for the CreateUserUseCase. The username is not part of it: it is generated.
 */
public record CreateUserCommand(
        String firstName,
        String lastName,
        UUID roleId,
        String password
) {
    @Override
    public String toString() {
        return "CreateUserCommand[firstName=" + firstName + ", lastName=" + lastName
                + ", roleId=" + roleId + ", password=protected]";
    }
}