package tsgp.yah_enterprise.inventory.infrastructure.persistence.repository;

import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockAdjustmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockAdjustmentJpaRepository extends JpaRepository<StockAdjustmentJpaEntity, UUID> {
}
