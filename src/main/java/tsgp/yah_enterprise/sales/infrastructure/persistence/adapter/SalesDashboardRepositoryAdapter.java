package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.sales.domain.repository.SalesDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SalesDashboardRepositoryAdapter implements SalesDashboardRepository {

    private final EntityManager em;

    @Override
    public long pendingOrderCount(Set<UUID> ids) {
        String sql = "SELECT COUNT(*) FROM public.orders o WHERE o.order_status = 'PENDING'"
                + scope("o.location_id", ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PendingOrderPage pendingOrders(Set<UUID> ids, int page, int size) {
        long total = pendingOrderCount(ids);
        String sql = "SELECT o.order_id, o.order_number, o.location_id, o.customer_id, o.total_amount, o.created_at"
                + " FROM public.orders o WHERE o.order_status = 'PENDING'" + scope("o.location_id", ids)
                + " ORDER BY o.created_at ASC, o.order_id ASC LIMIT :size OFFSET :offset";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("size", size);
        q.setParameter("offset", (long) page * size);
        List<Object[]> rows = q.getResultList();
        return new PendingOrderPage(rows.stream().map(r -> new PendingOrderRow((UUID) r[0], (String) r[1],
                (UUID) r[2], (UUID) r[3], num(r[4]), instant(r[5]))).toList(), total);
    }

    private static String scope(String column, Set<UUID> ids) {
        return ids == null ? "" : " AND " + column + " IN (:ids)";
    }

    private static Query bind(Query q, Set<UUID> ids) {
        if (ids != null) {
            q.setParameter("ids", ids);
        }
        return q;
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static Instant instant(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Instant i) {
            return i;
        }
        if (o instanceof java.time.OffsetDateTime d) {
            return d.toInstant();
        }
        if (o instanceof java.time.ZonedDateTime z) {
            return z.toInstant();
        }
        if (o instanceof java.sql.Timestamp t) {
            return t.toInstant();
        }
        if (o instanceof java.time.LocalDateTime l) {
            return l.toInstant(java.time.ZoneOffset.UTC);
        }
        throw new IllegalStateException("Unsupported timestamp type " + o.getClass());
    }
}
