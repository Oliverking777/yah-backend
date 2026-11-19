package tsgp.yah_enterprise.payment.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.payment.domain.repository.PaymentDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentDashboardRepositoryAdapter implements PaymentDashboardRepository {

    private final EntityManager em;

    @Override
    public long outstandingBalance(Set<UUID> ids) {
        String sql = "SELECT COALESCE(SUM(r.total_amount - r.allocated_amount), 0) FROM public.receivables r"
                + " WHERE r.total_amount > r.allocated_amount" + scope("r.location_id", ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    public long pendingCreditCount(Set<UUID> ids) {
        String sql = "SELECT COUNT(*) FROM public.credit_approvals ca JOIN public.orders o ON o.order_id = ca.sale_id"
                + " WHERE ca.approval_status = 'REQUESTED'" + scope("o.location_id", ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PendingCreditRow> pendingCredits(Set<UUID> ids, int limit) {
        String sql = "SELECT ca.credit_approval_id, ca.sale_id, o.location_id, ca.requested_at"
                + " FROM public.credit_approvals ca JOIN public.orders o ON o.order_id = ca.sale_id"
                + " WHERE ca.approval_status = 'REQUESTED'" + scope("o.location_id", ids)
                + " ORDER BY ca.requested_at ASC, ca.credit_approval_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new PendingCreditRow((UUID) r[0], (UUID) r[1], (UUID) r[2], instant(r[3])))
                .toList();
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
