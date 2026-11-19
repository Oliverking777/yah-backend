package tsgp.yah_enterprise.location.application.dto;

/**
 * Input for the UpdateLocationUseCase.
 * Any field left null keeps its current value. An empty or blank address removes the address.
 * The code never changes, and status changes go through activate/deactivate.
 */
public record UpdateLocationCommand(
        String name,
        String locationType,
        String address
) {
}