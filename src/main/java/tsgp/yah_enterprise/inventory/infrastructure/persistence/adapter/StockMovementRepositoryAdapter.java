package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;
import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;
import tsgp.yah_enterprise.inventory.domain.repository.ProductLocation;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementRepository;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper.StockMapper;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.repository.StockMovementJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryAdapter implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;
    private final StockMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public StockMovement save(StockMovement movement) {
        try {
            // saveAndFlush so a duplicate document line fails here, with a readable message
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(movement)));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidStockMovementException("This stock movement was already recorded");
        }
    }

    @Override
    public Optional<StockMovement> findByReference(ReferenceType referenceType, UUID referenceId, UUID productId,
                                                   UUID locationId, MovementType movementType) {
        return jpaRepository
                .findByReferenceTypeAndReferenceIdAndProductIdAndLocationIdAndMovementType(
                        referenceType, referenceId, productId, locationId, movementType)
                .map(mapper::toDomain);
    }

    @Override
    public List<StockMovement> findAll(StockMovementFilter filter, int page, int size) {
        TypedQuery<StockMovementJpaEntity> query = entityManager.createQuery(
                "select m from StockMovementJpaEntity m" + where(filter) + " order by m.recordedAt desc, m.movementId",
                StockMovementJpaEntity.class);
        bind(query, filter);
        return query.setFirstResult(page * size).setMaxResults(size)
                .getResultList().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(StockMovementFilter filter) {
        TypedQuery<Long> query = entityManager.createQuery(
                "select count(m) from StockMovementJpaEntity m" + where(filter), Long.class);
        bind(query, filter);
        return query.getSingleResult();
    }

    @Override
    public Set<ProductLocation> findWithMovements(Set<UUID> productIds, Set<UUID> locationIds) {
        if (productIds == null || productIds.isEmpty() || locationIds == null || locationIds.isEmpty()) {
            return Set.of();
        }
        // Served by idx_stock_movements_product_location (product_id, location_id, ...).
        List<Object[]> rows = entityManager.createQuery(
                        "select distinct m.productId, m.locationId from StockMovementJpaEntity m"
                                + " where m.productId in :productIds and m.locationId in :locationIds",
                        Object[].class)
                .setParameter("productIds", productIds)
                .setParameter("locationIds", locationIds)
                .getResultList();
        Set<ProductLocation> result = new HashSet<>();
        for (Object[] row : rows) {
            result.add(new ProductLocation((UUID) row[0], (UUID) row[1]));
        }
        return result;
    }

    private static String where(StockMovementFilter f) {
        StringBuilder sb = new StringBuilder(" where 1 = 1");
        if (f.productId() != null) sb.append(" and m.productId = :productId");
        if (f.locationId() != null) sb.append(" and m.locationId = :locationId");
        if (f.movementType() != null) sb.append(" and m.movementType = :movementType");
        if (f.from() != null) sb.append(" and m.recordedAt >= :from");
        if (f.to() != null) sb.append(" and m.recordedAt < :to");
        return sb.toString();
    }

    private static void bind(TypedQuery<?> query, StockMovementFilter f) {
        if (f.productId() != null) query.setParameter("productId", f.productId());
        if (f.locationId() != null) query.setParameter("locationId", f.locationId());
        if (f.movementType() != null) query.setParameter("movementType", f.movementType());
        if (f.from() != null) query.setParameter("from", f.from());
        if (f.to() != null) query.setParameter("to", f.to());
    }
}