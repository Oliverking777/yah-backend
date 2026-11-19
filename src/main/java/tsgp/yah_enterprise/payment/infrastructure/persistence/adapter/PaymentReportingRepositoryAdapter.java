package tsgp.yah_enterprise.payment.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.payment.domain.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentReportingRepositoryAdapter implements PaymentReportingRepository {

    private final EntityManager em;

    @Override
    public List<AmountRow> collected(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                     String zone) {
        String sql = "SELECT " + key("p.location_id", "p.received_at", groupBy) + " AS k, "
                + "COALESCE(SUM(p.amount), 0) FROM payments p "
                + "WHERE p.payment_status = 'RECORDED' AND p.received_at >= :from AND p.received_at < :to"
                + (locationIds == null ? "" : " AND p.location_id IN (:ids)") + " GROUP BY k ORDER BY k";
        return amounts(bind(em.createNativeQuery(sql), from, to, locationIds, groupBy, zone));
    }

    @Override
    public List<AmountRow> outstanding(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                       String zone) {
        // a receivable is created when the sale is completed on credit, so its creation date is the sale's date
        String sql = "SELECT " + key("r.location_id", "r.created_at", groupBy) + " AS k, "
                + "COALESCE(SUM(r.total_amount - r.allocated_amount), 0) FROM receivables r "
                + "WHERE r.created_at >= :from AND r.created_at < :to"
                + (locationIds == null ? "" : " AND r.location_id IN (:ids)") + " GROUP BY k ORDER BY k";
        return amounts(bind(em.createNativeQuery(sql), from, to, locationIds, groupBy, zone));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DebtRow> debtRows(Instant from, Instant to, Set<UUID> locationIds, UUID customerId,
                                  boolean onlyOutstanding) {
        String sql = "SELECT r.customer_id, r.location_id, SUM(r.total_amount), SUM(r.allocated_amount) "
                + "FROM receivables r WHERE r.created_at >= :from AND r.created_at < :to"
                + (locationIds == null ? "" : " AND r.location_id IN (:ids)")
                + (customerId == null ? "" : " AND r.customer_id = :customerId")
                + " GROUP BY r.customer_id, r.location_id"
                + (onlyOutstanding ? " HAVING SUM(r.total_amount) - SUM(r.allocated_amount) > 0" : "")
                + " ORDER BY (SUM(r.total_amount) - SUM(r.allocated_amount)) DESC, r.customer_id, r.location_id";
        Query q = em.createNativeQuery(sql).setParameter("from", from).setParameter("to", to);
        if (locationIds != null) q.setParameter("ids", locationIds);
        if (customerId != null) q.setParameter("customerId", customerId);
        return ((List<Object[]>) q.getResultList()).stream().map(r -> new DebtRow((UUID) r[0], (UUID) r[1],
                ((Number) r[2]).longValue(), ((Number) r[3]).longValue())).toList();
    }

    private static String key(String locationColumn, String timeColumn, TotalsGroupBy groupBy) {
        return switch (groupBy) {
            case LOCATION -> "CAST(" + locationColumn + " AS text)";
            case DAY -> "to_char(CAST((" + timeColumn + " AT TIME ZONE :zone) AS date), 'YYYY-MM-DD')";
            case NONE -> "'ALL'";
        };
    }

    private static Query bind(Query q, Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                              String zone) {
        q.setParameter("from", from).setParameter("to", to);
        if (groupBy == TotalsGroupBy.DAY) q.setParameter("zone", zone);
        if (locationIds != null) q.setParameter("ids", locationIds);
        return q;
    }

    @SuppressWarnings("unchecked")
    private static List<AmountRow> amounts(Query q) {
        return ((List<Object[]>) q.getResultList()).stream()
                .map(r -> new AmountRow(String.valueOf(r[0]), ((Number) r[1]).longValue())).toList();
    }
}
