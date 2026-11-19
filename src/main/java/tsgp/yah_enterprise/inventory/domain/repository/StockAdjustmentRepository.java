package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.StockAdjustment;
import tsgp.yah_enterprise.inventory.domain.valueobject.AdjustmentNumber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockAdjustmentRepository {

    StockAdjustment save(StockAdjustment adjustment);

    Optional<StockAdjustment> findById(UUID adjustmentId);

    /** Same as findById, but locks the row so two approvals of the same adjustment cannot both succeed. */
    Optional<StockAdjustment> findByIdForUpdate(UUID adjustmentId);

    /** Next human number from the database sequence (ADJ-000001...). */
    AdjustmentNumber nextNumber();

    /** Newest first; page is zero-based. */
    List<StockAdjustment> findAll(StockAdjustmentFilter filter, int page, int size);

    long count(StockAdjustmentFilter filter);
}
