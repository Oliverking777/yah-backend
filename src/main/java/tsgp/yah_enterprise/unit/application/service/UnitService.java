package tsgp.yah_enterprise.unit.application.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.unit.application.dto.CreateUnitCommand;
import tsgp.yah_enterprise.unit.application.dto.UnitPageRequest;
import tsgp.yah_enterprise.unit.application.dto.UnitPageResult;
import tsgp.yah_enterprise.unit.application.dto.UnitResult;
import tsgp.yah_enterprise.unit.application.dto.UpdateUnitCommand;
import tsgp.yah_enterprise.unit.application.port.in.ChangeUnitStatusUseCase;
import tsgp.yah_enterprise.unit.application.port.in.CreateUnitUseCase;
import tsgp.yah_enterprise.unit.application.port.in.GetUnitUseCase;
import tsgp.yah_enterprise.unit.application.port.in.GetUnitsByIdsUseCase;
import tsgp.yah_enterprise.unit.application.port.in.ListUnitsUseCase;
import tsgp.yah_enterprise.unit.application.port.in.UpdateUnitUseCase;
import tsgp.yah_enterprise.unit.domain.exception.InvalidUnitException;
import tsgp.yah_enterprise.unit.domain.exception.UnitNameAlreadyExistsException;
import tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException;
import tsgp.yah_enterprise.unit.domain.model.Unit;
import tsgp.yah_enterprise.unit.domain.model.UnitStatus;
import tsgp.yah_enterprise.unit.domain.port.UnitCodeGenerator;
import tsgp.yah_enterprise.unit.domain.repository.UnitRepository;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitCode;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitName;

/**
 * Application service implementing all unit use cases.
 * Who may call each use case is decided by the web/security layer.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UnitService implements
        CreateUnitUseCase,
        UpdateUnitUseCase,
        GetUnitUseCase,
        GetUnitsByIdsUseCase,
        ListUnitsUseCase,
        ChangeUnitStatusUseCase {

    private final UnitRepository unitRepository;
    private final UnitCodeGenerator unitCodeGenerator;

    /** A generated code can only be taken already by a unit made by hand in the same format: try the next one. */
    private static final int MAX_CODE_ATTEMPTS = 50;

    @Override
    public UnitResult createUnit(CreateUnitCommand command) {
        UnitName name = UnitName.of(command.unitName());

        // Friendly pre-check. The unique index in the database is the real guarantee.
        if (unitRepository.existsByName(name)) {
            throw new UnitNameAlreadyExistsException(name);
        }

        Unit unit = Unit.create(command.unitName(), nextFreeCode());
        return UnitResult.from(unitRepository.save(unit));
    }

    @Override
    public UnitResult updateUnit(UUID unitId, UpdateUnitCommand command) {
        Unit unit = findUnit(unitId);

        if (command.unitName() != null) {
            UnitName newName = UnitName.of(command.unitName());

            // The name may only clash with a *different* unit: renaming "carton" to "Carton" is allowed.
            boolean usedByAnother = unitRepository.findByName(newName)
                    .filter(other -> !other.getUnitId().equals(unitId))
                    .isPresent();
            if (usedByAnother) {
                throw new UnitNameAlreadyExistsException(newName);
            }
            if (!newName.equals(unit.getUnitName())) {
                unit.rename(command.unitName());
            }
        }

        return UnitResult.from(unitRepository.save(unit));
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResult getUnitById(UUID unitId) {
        return UnitResult.from(findUnit(unitId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResult> getUnitsByIds(Collection<UUID> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return List.of();
        }
        return unitRepository.findAllByIds(unitIds).stream().map(UnitResult::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitPageResult listUnits(UnitPageRequest request) {
        UnitStatus status = parseStatus(request.status());

        List<UnitResult> content = unitRepository.findAll(status, request.page(), request.size())
                .stream()
                .map(UnitResult::from)
                .toList();

        long total = unitRepository.count(status);
        return UnitPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public UnitResult activateUnit(UUID unitId) {
        Unit unit = findUnit(unitId);
        unit.activate();
        return UnitResult.from(unitRepository.save(unit));
    }

    @Override
    public UnitResult deactivateUnit(UUID unitId) {
        Unit unit = findUnit(unitId);
        unit.deactivate();
        return UnitResult.from(unitRepository.save(unit));
    }

    // ---- helpers ----

    private String nextFreeCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = unitCodeGenerator.nextCode();
            if (!unitRepository.existsByCode(UnitCode.of(code))) return code;
        }
        throw new IllegalStateException("No free unit code after " + MAX_CODE_ATTEMPTS + " attempts");
    }

    private Unit findUnit(UUID unitId) {
        return unitRepository.findById(unitId).orElseThrow(() -> new UnitNotFoundException(unitId));
    }

    private static UnitStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return UnitStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUnitException("Status must be ACTIVE or INACTIVE");
        }
    }
}
