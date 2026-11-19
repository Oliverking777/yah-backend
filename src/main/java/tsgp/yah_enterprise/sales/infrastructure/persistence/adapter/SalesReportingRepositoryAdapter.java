package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.sales.domain.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SalesReportingRepositoryAdapter implements SalesReportingRepository {

    private final EntityManager em;

    @Override
    public List<SalesTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, TotalsGroupBy groupBy,
                                       String zone) {
        String key = switch (groupBy) {
            case LOCATION -> "CAST(o.location_id AS text)";
            case DAY -> "to_char(CAST((o.completed_at AT TIME ZONE :zone) AS date), 'YYYY-MM-DD')";
            case NONE -> "'ALL'";
        };
        String sql = "SELECT " + key + " AS k, COUNT(*), COALESCE(SUM(o.subtotal), 0), "
                + "COALESCE(SUM(o.discount_total), 0) FROM orders o "
                + "WHERE o.order_status = 'COMPLETED' AND o.completed_at >= :from AND o.completed_at < :to"
                + (locationIds == null ? "" : " AND o.location_id IN (:ids)")
                + " GROUP BY k ORDER BY k";
        Query q = em.createNativeQuery(sql).setParameter("from", from).setParameter("to", to);
        if (groupBy == TotalsGroupBy.DAY) q.setParameter("zone", zone);
        if (locationIds != null) q.setParameter("ids", locationIds);
        return rows(q).stream().map(r -> new SalesTotalsRow(String.valueOf(r[0]), ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue(), ((Number) r[3]).longValue())).toList();
    }

    @Override
    public List<TopProductRow> topProducts(Instant from, Instant to, Set<UUID> locationIds, boolean byValue,
                                           int limit) {
        String sql = "SELECT i.product_id, SUM(CAST(i.quantity AS bigint) * i.conversion_factor) AS q, "
                + "SUM(i.line_total) AS v FROM order_items i JOIN orders o ON o.order_id = i.order_id "
                + "WHERE o.order_status = 'COMPLETED' AND o.completed_at >= :from AND o.completed_at < :to"
                + (locationIds == null ? "" : " AND o.location_id IN (:ids)")
                + " GROUP BY i.product_id ORDER BY " + (byValue ? "v" : "q") + " DESC, i.product_id LIMIT :limit";
        Query q = em.createNativeQuery(sql).setParameter("from", from).setParameter("to", to)
                .setParameter("limit", limit);
        if (locationIds != null) q.setParameter("ids", locationIds);
        return rows(q).stream().map(r -> new TopProductRow((UUID) r[0], ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue())).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<Object[]> rows(Query q) {
        return q.getResultList();
    }
}
