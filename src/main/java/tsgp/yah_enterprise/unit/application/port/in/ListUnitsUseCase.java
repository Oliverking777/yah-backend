package tsgp.yah_enterprise.unit.application.port.in;

import tsgp.yah_enterprise.unit.application.dto.UnitPageRequest;
import tsgp.yah_enterprise.unit.application.dto.UnitPageResult;

public interface ListUnitsUseCase {

    /**
     * One page of units ordered by name, optionally only ACTIVE or INACTIVE ones.
     *
     * @throws tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException if the status filter is not ACTIVE or INACTIVE
     */
    UnitPageResult listUnits(UnitPageRequest request);
}
