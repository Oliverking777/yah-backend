package tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper;

import tsgp.yah_enterprise.inventory.domain.model.StockAdjustment;
import tsgp.yah_enterprise.inventory.domain.model.StockLevel;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;
import tsgp.yah_enterprise.inventory.domain.valueobject.AdjustmentNumber;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockAdjustmentJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockLevelJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;
import org.springframework.stereotype.Component;

/** Entity <-> domain for the three Inventory aggregates. */
@Component
public class StockMapper {

    public StockLevel toDomain(StockLevelJpaEntity e) {
        return StockLevel.reconstitute(e.getStockLevelId(), e.getProductId(), e.getLocationId(), e.getQuantity(),
                e.getLowStockThreshold(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public StockLevelJpaEntity toEntity(StockLevel level) {
        StockLevelJpaEntity e = new StockLevelJpaEntity();
        e.setStockLevelId(level.getStockLevelId());
        e.setProductId(level.getProductId());
        e.setLocationId(level.getLocationId());
        e.setQuantity(level.getQuantity());
        e.setLowStockThreshold(level.getLowStockThreshold());
        e.setCreatedAt(level.getCreatedAt());
        e.setUpdatedAt(level.getUpdatedAt());
        return e;
    }

    public StockMovement toDomain(StockMovementJpaEntity e) {
        return StockMovement.reconstitute(e.getMovementId(), e.getProductId(), e.getLocationId(),
                e.getMovementType(), e.getQuantityDelta(), e.getBalanceAfter(), e.getReferenceType(),
                e.getReferenceId(), e.getPerformedBy(), e.getOccurredAt(), e.getRecordedAt(), e.getNote());
    }

    public StockMovementJpaEntity toEntity(StockMovement m) {
        StockMovementJpaEntity e = new StockMovementJpaEntity();
        e.setMovementId(m.getMovementId());
        e.setProductId(m.getProductId());
        e.setLocationId(m.getLocationId());
        e.setMovementType(m.getMovementType());
        e.setQuantityDelta(m.getQuantityDelta());
        e.setBalanceAfter(m.getBalanceAfter());
        e.setReferenceType(m.getReferenceType());
        e.setReferenceId(m.getReferenceId());
        e.setPerformedBy(m.getPerformedBy());
        e.setOccurredAt(m.getOccurredAt());
        e.setRecordedAt(m.getRecordedAt());
        e.setNote(m.getNote());
        return e;
    }

    public StockAdjustment toDomain(StockAdjustmentJpaEntity e) {
        return StockAdjustment.reconstitute(e.getAdjustmentId(), AdjustmentNumber.of(e.getAdjustmentNumber()),
                e.getProductId(), e.getLocationId(), e.getDirection(), e.getReasonCategory(), e.getQuantity(),
                e.getNote(), e.getAdjustmentStatus(), e.getCreatedBy(), e.getDecidedBy(), e.getDecisionNote(),
                e.getBeforeQuantity(), e.getAfterQuantity(), e.getCreatedAt(), e.getDecidedAt(), e.getUpdatedAt());
    }

    public StockAdjustmentJpaEntity toEntity(StockAdjustment a) {
        StockAdjustmentJpaEntity e = new StockAdjustmentJpaEntity();
        e.setAdjustmentId(a.getAdjustmentId());
        e.setAdjustmentNumber(a.getAdjustmentNumber().value());
        e.setProductId(a.getProductId());
        e.setLocationId(a.getLocationId());
        e.setDirection(a.getDirection());
        e.setReasonCategory(a.getReasonCategory());
        e.setQuantity(a.getQuantity());
        e.setNote(a.getNote());
        e.setAdjustmentStatus(a.getAdjustmentStatus());
        e.setCreatedBy(a.getCreatedBy());
        e.setDecidedBy(a.getDecidedBy());
        e.setDecisionNote(a.getDecisionNote());
        e.setBeforeQuantity(a.getBeforeQuantity());
        e.setAfterQuantity(a.getAfterQuantity());
        e.setCreatedAt(a.getCreatedAt());
        e.setDecidedAt(a.getDecidedAt());
        e.setUpdatedAt(a.getUpdatedAt());
        return e;
    }
}
