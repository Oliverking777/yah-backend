package tsgp.yah_enterprise.unit.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.unit.domain.exception.UnitCodeAlreadyExistsException;
import tsgp.yah_enterprise.unit.domain.exception.UnitNameAlreadyExistsException;
import tsgp.yah_enterprise.unit.domain.model.Unit;
import tsgp.yah_enterprise.unit.domain.model.UnitStatus;
import tsgp.yah_enterprise.unit.domain.repository.UnitRepository;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitCode;
import tsgp.yah_enterprise.unit.domain.valueobject.UnitName;
import tsgp.yah_enterprise.unit.infrastructure.persistence.mapper.UnitMapper;
import tsgp.yah_enterprise.unit.infrastructure.persistence.repository.UnitJpaRepository;

@Repository
@RequiredArgsConstructor
public class UnitRepositoryAdapter implements UnitRepository {

    static final String CODE_CONSTRAINT = "uq_units_unit_code";

    private final UnitJpaRepository jpaRepository;
    private final UnitMapper mapper;

    @Override
    public Unit save(Unit unit) {
        try {
            // saveAndFlush so a concurrent duplicate fails here, not later at commit time
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(unit)));
        } catch (DataIntegrityViolationException e) {
            // Two unique rules exist (name and code). We cannot run another query here: after a failed
            // statement PostgreSQL aborts the transaction. So we read the violated constraint's name.
            if (violatedConstraintIsCode(e)) {
                throw new UnitCodeAlreadyExistsException(unit.getUnitCode());
            }
            throw new UnitNameAlreadyExistsException(unit.getUnitName());
        }
    }

    public static boolean violatedConstraintIsCode(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains(CODE_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause() == cause ? null : cause.getCause();
        }
        return false;
    }

    @Override
    public Optional<Unit> findById(UUID unitId) {
        return jpaRepository.findById(unitId).map(mapper::toDomain);
    }

    @Override
    public List<Unit> findAllByIds(Collection<UUID> unitIds) {
        return jpaRepository.findAllById(unitIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Unit> findByName(UnitName unitName) {
        return jpaRepository.findByUnitNameIgnoreCase(unitName.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Unit> findByCode(UnitCode unitCode) {
        return jpaRepository.findByUnitCodeIgnoreCase(unitCode.value()).map(mapper::toDomain);
    }

    @Override
    public List<Unit> findAll(UnitStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        var result = status == null
                ? jpaRepository.findAllByOrderByUnitNameAsc(pageRequest)
                : jpaRepository.findAllByUnitStatusOrderByUnitNameAsc(status, pageRequest);
        return result.getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(UnitStatus status) {
        return status == null ? jpaRepository.count() : jpaRepository.countByUnitStatus(status);
    }

    @Override
    public boolean existsByName(UnitName unitName) {
        return jpaRepository.existsByUnitNameIgnoreCase(unitName.value());
    }

    @Override
    public boolean existsByCode(UnitCode unitCode) {
        return jpaRepository.existsByUnitCodeIgnoreCase(unitCode.value());
    }
}
