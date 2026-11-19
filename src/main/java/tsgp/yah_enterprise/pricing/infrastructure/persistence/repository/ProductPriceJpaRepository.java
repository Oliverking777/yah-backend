package tsgp.yah_enterprise.pricing.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.pricing.domain.model.PriceStatus;
import tsgp.yah_enterprise.pricing.infrastructure.persistence.entity.ProductPriceJpaEntity;

import java.util.List;
import java.util.UUID;

public interface ProductPriceJpaRepository extends JpaRepository<ProductPriceJpaEntity, UUID> {

    List<ProductPriceJpaEntity> findByProductIdAndUnitIdAndPriceStatus(UUID productId, UUID unitId, PriceStatus priceStatus);

    List<ProductPriceJpaEntity> findByProductIdAndPriceStatus(UUID productId, PriceStatus priceStatus);

    // History: two explicit methods instead of one "unit is optional" query,
    // because a null UUID parameter in a JPQL condition is unreliable with PostgreSQL.
    Page<ProductPriceJpaEntity> findByProductId(UUID productId, Pageable pageable);

    Page<ProductPriceJpaEntity> findByProductIdAndUnitId(UUID productId, UUID unitId, Pageable pageable);

    long countByProductId(UUID productId);

    long countByProductIdAndUnitId(UUID productId, UUID unitId);
}
