package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import tsgp.yah_enterprise.inventory.domain.model.StockLevel;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelRepository;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockLevelJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper.StockMapper;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.repository.StockLevelJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockLevelRepositoryAdapter implements StockLevelRepository {

    private final StockLevelJpaRepository jpaRepository;
    private final StockMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public StockLevel save(StockLevel stockLevel) {
        return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(stockLevel)));
    }

    @Override
    public Optional<StockLevel> findByProductAndLocation(UUID productId, UUID locationId) {
        return jpaRepository.findByProductIdAndLocationId(productId, locationId).map(mapper::toDomain);
    }

    @Override
    public StockLevel lockOrCreate(UUID productId, UUID locationId) {
        // The insert is a no-op when the row exists, so two first-time callers never fail on the unique key
        // (a failed insert would abort the whole PostgreSQL transaction). The select below then locks the row.
        Instant now = Instant.now();
        entityManager.createNativeQuery("""
                        INSERT INTO public.stock_levels
                            (stock_level_id, product_id, location_id, quantity, low_stock_threshold, created_at, updated_at)
                        VALUES (:id, :productId, :locationId, 0, NULL, :now, :now)
                        ON CONFLICT (product_id, location_id) DO NOTHING
                        """)
                .setParameter("id", UUID.randomUUID())
                .setParameter("productId", productId)
                .setParameter("locationId", locationId)
                .setParameter("now", now)
                .executeUpdate();

        StockLevelJpaEntity locked = entityManager.createQuery(
                        "select s from StockLevelJpaEntity s where s.productId = :productId and s.locationId = :locationId",
                        StockLevelJpaEntity.class)
                .setParameter("productId", productId)
                .setParameter("locationId", locationId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
        return mapper.toDomain(locked);
    }

    @Override
    public List<StockLevel> findAll(StockLevelFilter filter, int page, int size) {
        TypedQuery<StockLevelJpaEntity> query = entityManager.createQuery(
                "select s from StockLevelJpaEntity s" + where(filter) + " order by s.productId, s.locationId",
                StockLevelJpaEntity.class);
        bind(query, filter);
        return query.setFirstResult(page * size).setMaxResults(size)
                .getResultList().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(StockLevelFilter filter) {
        TypedQuery<Long> query = entityManager.createQuery(
                "select count(s) from StockLevelJpaEntity s" + where(filter), Long.class);
        bind(query, filter);
        return query.getSingleResult();
    }

    // Optional filters are added to the query text only when present: a null UUID parameter in a JPQL condition
    // is unreliable with PostgreSQL.
    private static String where(StockLevelFilter f) {
        StringBuilder sb = new StringBuilder(" where 1 = 1");
        if (f.locationId() != null) sb.append(" and s.locationId = :locationId");
        if (f.productId() != null) sb.append(" and s.productId = :productId");
        if (f.lowStockOnly()) sb.append(" and s.lowStockThreshold is not null and s.quantity <= s.lowStockThreshold");
        return sb.toString();
    }

    private static void bind(TypedQuery<?> query, StockLevelFilter f) {
        if (f.locationId() != null) query.setParameter("locationId", f.locationId());
        if (f.productId() != null) query.setParameter("productId", f.productId());
    }
}
