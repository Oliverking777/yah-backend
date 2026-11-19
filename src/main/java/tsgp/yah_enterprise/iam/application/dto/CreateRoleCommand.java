package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the CreateRoleUseCase.
 * Plain data: the domain (RoleName / Role) validates the values.
 * locationScope is "ALL" or "ASSIGNED"; null means ASSIGNED.
 */
public record CreateRoleCommand(
        String roleName,
        String description,
        String locationScope
) {}