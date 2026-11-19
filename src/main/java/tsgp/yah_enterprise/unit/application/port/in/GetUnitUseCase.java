package tsgp.yah_enterprise.unit.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.unit.application.dto.UnitResult;

/** Called by the Product module. Do not change this signature once other modules use it. */
public interface GetUnitUseCase {

    /** @throws tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException if the unit does not exist */
    UnitResult getUnitById(UUID unitId);
}
