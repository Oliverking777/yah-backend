package tsgp.yah_enterprise.unit.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.unit.application.dto.UnitResult;
import tsgp.yah_enterprise.unit.application.dto.UpdateUnitCommand;

public interface UpdateUnitUseCase {

    /**
     * Renames a unit. A null name changes nothing. The code and the status are not changed here.
     *
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException           if the unit does not exist
     * @throws tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException            if the name is invalid
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitNameAlreadyExistsException  if another unit already uses the name
     */
    UnitResult updateUnit(UUID unitId, UpdateUnitCommand command);
}
