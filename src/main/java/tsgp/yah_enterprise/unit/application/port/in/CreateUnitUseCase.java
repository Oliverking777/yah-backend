package tsgp.yah_enterprise.unit.application.port.in;

import tsgp.yah_enterprise.unit.application.dto.CreateUnitCommand;
import tsgp.yah_enterprise.unit.application.dto.UnitResult;

public interface CreateUnitUseCase {

    /**
     * Creates a new, active unit.
     *
     * @throws tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException            if the name or code is invalid
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitNameAlreadyExistsException  if the name is taken (ignoring case)
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitCodeAlreadyExistsException  if the code is taken
     */
    UnitResult createUnit(CreateUnitCommand command);
}
