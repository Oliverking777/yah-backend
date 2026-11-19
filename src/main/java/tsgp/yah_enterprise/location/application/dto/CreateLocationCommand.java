package tsgp.yah_enterprise.location.application.dto;

/**
 * Input for the CreateLocationUseCase.
 * Plain strings, like the IAM commands: the service turns the type into a LocationType.
 * There is no code: the service generates it. address is optional (null or blank = none).
 */
public record CreateLocationCommand(
        String name,
        String locationType,
        String address
) {
}