package tsgp.yah_enterprise.inventory.infrastructure.persistence.repository;

import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockInitializationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockInitializationJpaRepository extends JpaRepository<StockInitializationJpaEntity, UUID> {
}
