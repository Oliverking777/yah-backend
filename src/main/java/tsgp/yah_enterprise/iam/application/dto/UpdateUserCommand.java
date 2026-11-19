package tsgp.yah_enterprise.iam.application.dto;

import java.util.UUID;

/**
 * Input for the UpdateUserUseCase.
 * A null field means "leave unchanged". The username cannot be changed.
 */
public record UpdateUserCommand(
        String firstName,
        String lastName,
        UUID roleId
) {}