package tsgp.yah_enterprise.inventory.infrastructure.persistence.repository;

import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {

    Optional<StockMovementJpaEntity> findByReferenceTypeAndReferenceIdAndProductIdAndLocationIdAndMovementType(
            ReferenceType referenceType, UUID referenceId, UUID productId, UUID locationId, MovementType movementType);
}
