package tsgp.yah_enterprise.unit.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.unit.application.dto.UnitResult;

public interface ChangeUnitStatusUseCase {

    /**
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException        if the unit does not exist
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyActiveException   if it is already active
     */
    UnitResult activateUnit(UUID unitId);

    /**
     * Retires a unit. It is kept, not deleted. It only blocks new assignments:
     * product units that already use it keep working.
     *
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException          if the unit does not exist
     * @throws tsgp.yah_enterprise.unit.domain.exception.UnitAlreadyInactiveException   if it is already inactive
     */
    UnitResult deactivateUnit(UUID unitId);
}
