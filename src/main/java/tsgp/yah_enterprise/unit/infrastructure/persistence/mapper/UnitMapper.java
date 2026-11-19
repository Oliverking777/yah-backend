package tsgp.yah_enterprise.unit.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.unit.domain.model.Unit;
import tsgp.yah_enterprise.unit.infrastructure.persistence.entity.UnitJpaEntity;

@Component
public class UnitMapper {

    public Unit toDomain(UnitJpaEntity entity) {
        return Unit.reconstitute(
                entity.getUnitId(),
                entity.getUnitName(),
                entity.getUnitCode(),
                entity.getUnitStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public UnitJpaEntity toEntity(Unit unit) {
        UnitJpaEntity entity = new UnitJpaEntity();
        entity.setUnitId(unit.getUnitId());
        entity.setUnitName(unit.getUnitName().value());
        entity.setUnitCode(unit.getUnitCode().value());
        entity.setUnitStatus(unit.getUnitStatus());
        entity.setCreatedAt(unit.getCreatedAt());
        entity.setUpdatedAt(unit.getUpdatedAt());
        return entity;
    }
}
