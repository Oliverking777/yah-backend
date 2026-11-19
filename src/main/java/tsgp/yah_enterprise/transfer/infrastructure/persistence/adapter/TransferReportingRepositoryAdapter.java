package tsgp.yah_enterprise.transfer.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.transfer.domain.repository.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransferReportingRepositoryAdapter implements TransferReportingRepository {

    private static final String RECEIVED = "t.transfer_status = 'RECEIVED'";

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<TransferTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, UUID sourceLocationId,
                                          UUID destinationLocationId) {
        String scope = (locationIds == null ? ""
                : " AND (t.source_location_id IN (:ids) OR t.destination_location_id IN (:ids))")
                + (sourceLocationId == null ? "" : " AND t.source_location_id = :source")
                + (destinationLocationId == null ? "" : " AND t.destination_location_id = :destination");
        String sql = "SELECT t.source_location_id, t.destination_location_id, "
                + "COALESCE(SUM(CAST(i.quantity AS bigint) * i.conversion_factor), 0), "
                + "COALESCE(SUM(CASE WHEN " + RECEIVED + " THEN CAST(COALESCE(i.received_quantity, 0) AS bigint) * i.conversion_factor ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN " + RECEIVED + " THEN CAST(COALESCE(i.damaged_quantity, 0) AS bigint) * i.conversion_factor ELSE 0 END), 0), "
                + "COALESCE(SUM(CASE WHEN " + RECEIVED + " THEN CAST(i.quantity - COALESCE(i.received_quantity, 0) - COALESCE(i.damaged_quantity, 0) AS bigint) * i.conversion_factor ELSE 0 END), 0), "
                + "COUNT(DISTINCT CASE WHEN t.has_discrepancy THEN t.transfer_id END) "
                + "FROM transfers t JOIN transfer_items i ON i.transfer_id = t.transfer_id "
                + "WHERE t.transfer_status IN ('SUBMITTED', 'RECEIVED') "
                + "AND t.created_at >= :from AND t.created_at < :to" + scope
                + " GROUP BY t.source_location_id, t.destination_location_id";
        Query q = em.createNativeQuery(sql).setParameter("from", from).setParameter("to", to);
        bindScope(q, locationIds, sourceLocationId, destinationLocationId);
        Map<String, Object[]> totals = new LinkedHashMap<>();
        ((List<Object[]>) q.getResultList()).forEach(r -> totals.put(r[0] + ">" + r[1], r));

        String pendingSql = "SELECT t.source_location_id, t.destination_location_id, COUNT(*) FROM transfers t "
                + "WHERE t.transfer_status = 'SUBMITTED'" + scope
                + " GROUP BY t.source_location_id, t.destination_location_id";
        Query p = em.createNativeQuery(pendingSql);
        bindScope(p, locationIds, sourceLocationId, destinationLocationId);
        List<Object[]> pendingRows = p.getResultList();
        Map<String, Long> pending = new LinkedHashMap<>();
        pendingRows.forEach(r -> pending.put(r[0] + ">" + r[1], ((Number) r[2]).longValue()));

        // a route with only pending transfers still appears, with zero quantities
        Map<String, UUID[]> routes = new LinkedHashMap<>();
        totals.forEach((k, r) -> routes.put(k, new UUID[] {(UUID) r[0], (UUID) r[1]}));
        pendingRows.forEach(r -> routes.putIfAbsent(r[0] + ">" + r[1], new UUID[] {(UUID) r[0], (UUID) r[1]}));
        return routes.entrySet().stream().map(e -> {
            Object[] r = totals.get(e.getKey());
            return new TransferTotalsRow(e.getValue()[0], e.getValue()[1],
                    r == null ? 0 : ((Number) r[2]).longValue(), r == null ? 0 : ((Number) r[3]).longValue(),
                    r == null ? 0 : ((Number) r[4]).longValue(), r == null ? 0 : ((Number) r[5]).longValue(),
                    pending.getOrDefault(e.getKey(), 0L), r == null ? 0 : ((Number) r[6]).longValue());
        }).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InTransitRow> inTransit(Set<UUID> destinationLocationIds) {
        String sql = "SELECT t.destination_location_id, i.product_id, "
                + "SUM(CAST(i.quantity AS bigint) * i.conversion_factor) "
                + "FROM transfers t JOIN transfer_items i ON i.transfer_id = t.transfer_id "
                + "WHERE t.transfer_status = 'SUBMITTED'"
                + (destinationLocationIds == null ? "" : " AND t.destination_location_id IN (:ids)")
                + " GROUP BY t.destination_location_id, i.product_id";
        Query q = em.createNativeQuery(sql);
        if (destinationLocationIds != null) q.setParameter("ids", destinationLocationIds);
        return ((List<Object[]>) q.getResultList()).stream().map(r -> new InTransitRow((UUID) r[0], (UUID) r[1],
                ((Number) r[2]).longValue())).toList();
    }

    private static void bindScope(Query q, Set<UUID> locationIds, UUID source, UUID destination) {
        if (locationIds != null) q.setParameter("ids", locationIds);
        if (source != null) q.setParameter("source", source);
        if (destination != null) q.setParameter("destination", destination);
    }
}