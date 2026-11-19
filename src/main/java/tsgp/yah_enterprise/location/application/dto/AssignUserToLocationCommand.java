package tsgp.yah_enterprise.location.application.dto;

import java.util.UUID;

/**
 * Input for the AssignUserToLocationUseCase.
 * The location id is passed separately, as in the IAM update use cases.
 */
public record AssignUserToLocationCommand(
        UUID userId
) {
}