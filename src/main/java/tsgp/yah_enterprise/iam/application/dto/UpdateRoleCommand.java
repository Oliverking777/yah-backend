package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the UpdateRoleUseCase.
 * A null field means "leave unchanged".
 */
public record UpdateRoleCommand(
        String roleName,
        String description,
        String locationScope
) {}