package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;
import tsgp.yah_enterprise.inventory.domain.model.StockInitialization;
import tsgp.yah_enterprise.inventory.domain.model.StockInitializationStatus;
import tsgp.yah_enterprise.inventory.domain.repository.StockInitializationFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockInitializationRepository;
import tsgp.yah_enterprise.inventory.domain.valueobject.InitializationNumber;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockInitializationJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper.StockInitializationMapper;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.repository.StockInitializationJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
public class StockInitializationRepositoryAdapter implements StockInitializationRepository {

    private final StockInitializationJpaRepository jpaRepository;
    private final StockInitializationMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public StockInitialization save(StockInitialization initialization) {
        try {
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(initialization)));
        } catch (DataIntegrityViolationException e) {
            // The unique constraint uq_stock_init_items_location_product fired: a concurrent
            // request already initialized one of these products at this location.
            throw new InvalidStockInitializationException(
                    "One of the products has already been initialized at this location (concurrent conflict)");
        }
    }

    @Override
    public Optional<StockInitialization> findById(UUID initializationId) {
        return jpaRepository.findById(initializationId).map(mapper::toDomain);
    }

    @Override
    public Optional<StockInitialization> findByIdForUpdate(UUID initializationId) {
        return Optional.ofNullable(
                        entityManager.find(StockInitializationJpaEntity.class, initializationId,
                                LockModeType.PESSIMISTIC_WRITE))
                .map(mapper::toDomain);
    }

    @Override
    public InitializationNumber nextNumber() {
        Number next = (Number) entityManager
                .createNativeQuery("select nextval('public.stock_initialization_number_seq')")
                .getSingleResult();
        return InitializationNumber.fromSequence(next.longValue());
    }

    @Override
    public List<StockInitialization> findAll(StockInitializationFilter filter, int page, int size) {
        TypedQuery<StockInitializationJpaEntity> query = entityManager.createQuery(
                "select i from StockInitializationJpaEntity i" + where(filter)
                        + " order by i.createdAt desc, i.initializationId",
                StockInitializationJpaEntity.class);
        bind(query, filter);
        return query.setFirstResult(page * size).setMaxResults(size)
                .getResultList().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(StockInitializationFilter filter) {
        TypedQuery<Long> query = entityManager.createQuery(
                "select count(i) from StockInitializationJpaEntity i" + where(filter), Long.class);
        bind(query, filter);
        return query.getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<UUID> findInitializedProductIds(UUID locationId, Set<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Set.of();
        }
        List<UUID> results = entityManager.createNativeQuery(
                        "select distinct i.product_id from public.stock_initialization_items i "
                                + "where i.location_id = :locationId and i.product_id in :productIds")
                .setParameter("locationId", locationId)
                .setParameter("productIds", productIds)
                .getResultList();
        return new HashSet<>(results);
    }

    private static String where(StockInitializationFilter f) {
        StringBuilder sb = new StringBuilder(" where 1 = 1");
        if (f.status() != null) sb.append(" and i.status = :status");
        if (f.locationId() != null) sb.append(" and i.locationId = :locationId");
        return sb.toString();
    }

    private static void bind(TypedQuery<?> query, StockInitializationFilter f) {
        if (f.status() != null) query.setParameter("status", f.status());
        if (f.locationId() != null) query.setParameter("locationId", f.locationId());
    }
}
