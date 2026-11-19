package tsgp.yah_enterprise.unit.application.dto;

/**
 * Input for the UpdateUnitUseCase. Only the name can change.
 * The code never changes and the status is changed with activate/deactivate.
 * A null name keeps the current one.
 */
public record UpdateUnitCommand(
        String unitName
) {
}
