package tsgp.yah_enterprise.unit.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import tsgp.yah_enterprise.unit.application.dto.UnitResult;

/** Batch read for the Product module (one query instead of one per unit). Do not change this signature. */
public interface GetUnitsByIdsUseCase {

    /** Unknown ids are skipped, no exception is thrown. A null or empty input gives an empty list. */
    List<UnitResult> getUnitsByIds(Collection<UUID> unitIds);
}
