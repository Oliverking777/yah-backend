package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.inventory.domain.repository.StockDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockDashboardRepositoryAdapter implements StockDashboardRepository {

    private final EntityManager em;

    @Override
    public StockCounts counts(Set<UUID> ids) {
        // In stock is the exact complement of low and out, so the three always add up to every line in scope.
        String sql = "SELECT COALESCE(SUM(CASE WHEN s.quantity > 0 AND (s.low_stock_threshold IS NULL"
                + " OR s.quantity > s.low_stock_threshold) THEN 1 ELSE 0 END), 0),"
                + " COALESCE(SUM(CASE WHEN s.quantity > 0 AND s.low_stock_threshold IS NOT NULL"
                + " AND s.quantity <= s.low_stock_threshold THEN 1 ELSE 0 END), 0),"
                + " COALESCE(SUM(CASE WHEN s.quantity <= 0 THEN 1 ELSE 0 END), 0)"
                + " FROM public.stock_levels s WHERE 1 = 1" + scope("s.location_id", ids);
        Object[] r = (Object[]) bind(em.createNativeQuery(sql), ids).getSingleResult();
        return new StockCounts(num(r[0]), num(r[1]), num(r[2]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StockAlertRow> lowAndOut(Set<UUID> ids, int limit) {
        String sql = "SELECT s.product_id, s.location_id, s.quantity, s.low_stock_threshold"
                + " FROM public.stock_levels s WHERE (s.quantity <= 0 OR (s.low_stock_threshold IS NOT NULL"
                + " AND s.quantity <= s.low_stock_threshold))" + scope("s.location_id", ids)
                + " ORDER BY CASE WHEN s.quantity <= 0 THEN 0 ELSE 1 END, s.quantity ASC, s.product_id ASC,"
                + " s.location_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new StockAlertRow((UUID) r[0], (UUID) r[1], num(r[2]),
                r[3] == null ? null : num(r[3]))).toList();
    }

    @Override
    public long pendingAdjustmentCount(Set<UUID> ids) {
        String sql = "SELECT COUNT(*) FROM public.stock_adjustments a WHERE a.adjustment_status = 'PENDING'"
                + scope("a.location_id", ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PendingAdjustmentRow> pendingAdjustments(Set<UUID> ids, int limit) {
        String sql = "SELECT a.adjustment_id, a.adjustment_number, a.location_id, a.created_at"
                + " FROM public.stock_adjustments a WHERE a.adjustment_status = 'PENDING'"
                + scope("a.location_id", ids) + " ORDER BY a.created_at ASC, a.adjustment_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new PendingAdjustmentRow((UUID) r[0], (String) r[1], (UUID) r[2],
                instant(r[3]))).toList();
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