package tsgp.yah_enterprise.inventory.infrastructure.persistence.repository;

import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockLevelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockLevelJpaRepository extends JpaRepository<StockLevelJpaEntity, UUID> {

    Optional<StockLevelJpaEntity> findByProductIdAndLocationId(UUID productId, UUID locationId);
}
