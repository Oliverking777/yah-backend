package tsgp.yah_enterprise.unit.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.unit.domain.model.Unit;
import tsgp.yah_enterprise.unit.domain.model.UnitStatus;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitCode;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitName;

/**
 * Repository interface (port) for units. The implementation lives in the infrastructure layer.
 * Name and code lookups ignore case.
 */
public interface UnitRepository {

    /** Saves a new unit or updates an existing one. */
    Unit save(Unit unit);

    Optional<Unit> findById(UUID unitId);

    /** Batch read. Unknown ids are skipped. */
    List<Unit> findAllByIds(Collection<UUID> unitIds);

    Optional<Unit> findByName(UnitName unitName);

    Optional<Unit> findByCode(UnitCode unitCode);

    /**
     * One page of units ordered by name.
     *
     * @param status only units with this status, or null for all
     * @param page   zero-based page index
     */
    List<Unit> findAll(UnitStatus status, int page, int size);

    /** Number of units with this status, or of all units when status is null. */
    long count(UnitStatus status);

    boolean existsByName(UnitName unitName);

    boolean existsByCode(UnitCode unitCode);
}
