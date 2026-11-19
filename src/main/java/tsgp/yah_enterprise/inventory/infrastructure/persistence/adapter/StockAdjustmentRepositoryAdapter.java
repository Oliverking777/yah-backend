package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import tsgp.yah_enterprise.inventory.domain.model.StockAdjustment;
import tsgp.yah_enterprise.inventory.domain.repository.StockAdjustmentFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockAdjustmentRepository;
import tsgp.yah_enterprise.inventory.domain.valueobject.AdjustmentNumber;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockAdjustmentJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper.StockMapper;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.repository.StockAdjustmentJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockAdjustmentRepositoryAdapter implements StockAdjustmentRepository {

    private final StockAdjustmentJpaRepository jpaRepository;
    private final StockMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public StockAdjustment save(StockAdjustment adjustment) {
        return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(adjustment)));
    }

    @Override
    public Optional<StockAdjustment> findById(UUID adjustmentId) {
        return jpaRepository.findById(adjustmentId).map(mapper::toDomain);
    }

    @Override
    public Optional<StockAdjustment> findByIdForUpdate(UUID adjustmentId) {
        return Optional.ofNullable(
                        entityManager.find(StockAdjustmentJpaEntity.class, adjustmentId, LockModeType.PESSIMISTIC_WRITE))
                .map(mapper::toDomain);
    }

    @Override
    public AdjustmentNumber nextNumber() {
        Number next = (Number) entityManager
                .createNativeQuery("select nextval('public.stock_adjustment_number_seq')")
                .getSingleResult();
        return AdjustmentNumber.fromSequence(next.longValue());
    }

    @Override
    public List<StockAdjustment> findAll(StockAdjustmentFilter filter, int page, int size) {
        TypedQuery<StockAdjustmentJpaEntity> query = entityManager.createQuery(
                "select a from StockAdjustmentJpaEntity a" + where(filter) + " order by a.createdAt desc, a.adjustmentId",
                StockAdjustmentJpaEntity.class);
        bind(query, filter);
        return query.setFirstResult(page * size).setMaxResults(size)
                .getResultList().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(StockAdjustmentFilter filter) {
        TypedQuery<Long> query = entityManager.createQuery(
                "select count(a) from StockAdjustmentJpaEntity a" + where(filter), Long.class);
        bind(query, filter);
        return query.getSingleResult();
    }

    private static String where(StockAdjustmentFilter f) {
        StringBuilder sb = new StringBuilder(" where 1 = 1");
        if (f.status() != null) sb.append(" and a.adjustmentStatus = :status");
        if (f.locationId() != null) sb.append(" and a.locationId = :locationId");
        if (f.productId() != null) sb.append(" and a.productId = :productId");
        return sb.toString();
    }

    private static void bind(TypedQuery<?> query, StockAdjustmentFilter f) {
        if (f.status() != null) query.setParameter("status", f.status());
        if (f.locationId() != null) query.setParameter("locationId", f.locationId());
        if (f.productId() != null) query.setParameter("productId", f.productId());
    }
}
