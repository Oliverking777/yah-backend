package tsgp.yah_enterprise.transfer.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.transfer.domain.repository.TransferDashboardRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransferDashboardRepositoryAdapter implements TransferDashboardRepository {

    private final EntityManager em;

    private static String inScope(Set<UUID> ids) {
        return ids == null ? "" : " AND (t.source_location_id IN (:ids) OR t.destination_location_id IN (:ids))";
    }

    @Override
    public long pendingCount(Set<UUID> ids) {
        String sql = "SELECT COUNT(*) FROM public.transfers t WHERE t.transfer_status = 'SUBMITTED'" + inScope(ids);
        return num(bind(em.createNativeQuery(sql), ids).getSingleResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PendingTransferPage pending(Set<UUID> ids, int page, int size) {
        long total = pendingCount(ids);
        String sql = "SELECT t.transfer_id, t.transfer_number, t.source_location_id, t.destination_location_id,"
                + " t.created_at FROM public.transfers t"
                + " WHERE t.transfer_status = 'SUBMITTED'" + inScope(ids)
                + " ORDER BY t.created_at ASC, t.transfer_id ASC"
                + " LIMIT :size OFFSET :offset";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("size", size);
        q.setParameter("offset", (long) page * size);
        List<Object[]> rows = q.getResultList();
        return new PendingTransferPage(rows.stream().map(r -> new PendingTransferRow((UUID) r[0], (String) r[1],
                (UUID) r[2], (UUID) r[3], instant(r[4]))).toList(), total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DiscrepancyTransferRow> discrepancyTransfers(Set<UUID> ids, Instant since, int limit) {
        String sql = "SELECT t.transfer_id, t.transfer_number, t.source_location_id, t.destination_location_id,"
                + " t.received_at FROM public.transfers t WHERE t.transfer_status = 'RECEIVED'"
                + " AND t.has_discrepancy AND t.received_at >= :since" + inScope(ids)
                + " ORDER BY t.received_at DESC, t.transfer_id ASC LIMIT :limit";
        Query q = bind(em.createNativeQuery(sql), ids);
        q.setParameter("since", since);
        q.setParameter("limit", limit);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new DiscrepancyTransferRow((UUID) r[0], (String) r[1], (UUID) r[2],
                (UUID) r[3], instant(r[4]))).toList();
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