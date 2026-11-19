package tsgp.yah_enterprise.purchasing.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingDashboardRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PurchasingDashboardRepositoryAdapter implements PurchasingDashboardRepository {

    private final EntityManager em;

    @Override
    public long awaitingDeliveryCount(Set<UUID> ids) {
        String sql = "SELECT COUNT(*) FROM public.purchases p WHERE p.purchase_status = 'SUBMITTED'"
                + scope("p.destination_location_id", ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AwaitingDeliveryRow> awaitingDelivery(Set<UUID> ids, int limit) {
        String sql = "SELECT p.purchase_id, p.purchase_number, p.supplier_id, p.destination_location_id,"
                + " p.expected_delivery_date, p.created_at FROM public.purchases p"
                + " WHERE p.purchase_status = 'SUBMITTED'" + scope("p.destination_location_id", ids)
                + " ORDER BY p.expected_delivery_date ASC NULLS LAST, p.created_at ASC, p.purchase_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new AwaitingDeliveryRow((UUID) r[0], (String) r[1], (UUID) r[2], (UUID) r[3],
                date(r[4]), instant(r[5]))).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DiscrepancyPurchaseRow> discrepancyPurchases(Set<UUID> ids, Instant since, int limit) {
        String sql = "SELECT p.purchase_id, p.purchase_number, p.supplier_id, p.destination_location_id, p.confirmed_at"
                + " FROM public.purchases p WHERE p.purchase_status = 'CONFIRMED' AND p.has_discrepancy"
                + " AND p.confirmed_at >= :since" + scope("p.destination_location_id", ids)
                + " ORDER BY p.confirmed_at DESC, p.purchase_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("since", since);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new DiscrepancyPurchaseRow((UUID) r[0], (String) r[1], (UUID) r[2],
                (UUID) r[3], instant(r[4]))).toList();
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

    private static LocalDate date(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDate d) {
            return d;
        }
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        throw new IllegalStateException("Unsupported date type " + o.getClass());
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