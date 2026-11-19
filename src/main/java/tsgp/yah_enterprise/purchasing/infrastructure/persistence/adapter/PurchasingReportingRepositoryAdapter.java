package tsgp.yah_enterprise.purchasing.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingReportingRepository;
import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingTotalsRow;

import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PurchasingReportingRepositoryAdapter implements PurchasingReportingRepository {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<PurchasingTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, UUID supplierId) {
        String scope = (locationIds == null ? "" : " AND p.destination_location_id IN (:ids)")
                + (supplierId == null ? "" : " AND p.supplier_id = :supplierId");

        // what was ordered in the period (cancelled purchases left out)
        Query ordered = em.createNativeQuery("SELECT p.supplier_id, COUNT(DISTINCT p.purchase_id), "
                + "SUM(CAST(i.ordered_quantity AS bigint) * i.conversion_factor), SUM(i.line_total) "
                + "FROM purchases p JOIN purchase_items i ON i.purchase_id = p.purchase_id "
                + "WHERE p.purchase_status <> 'CANCELLED' AND p.created_at >= :from AND p.created_at < :to"
                + scope + " GROUP BY p.supplier_id");
        // what was delivered in the period
        Query delivered = em.createNativeQuery("SELECT p.supplier_id, "
                + "SUM(CAST(COALESCE(i.accepted_quantity, 0) AS bigint) * i.conversion_factor), "
                + "SUM(CAST(COALESCE(i.rejected_quantity, 0) AS bigint) * i.conversion_factor), "
                + "SUM(CAST(COALESCE(i.accepted_quantity, 0) AS bigint) * i.unit_purchase_price), "
                + "COUNT(DISTINCT CASE WHEN p.has_discrepancy THEN p.purchase_id END) "
                + "FROM purchases p JOIN purchase_items i ON i.purchase_id = p.purchase_id "
                + "WHERE p.purchase_status = 'CONFIRMED' AND p.confirmed_at >= :from AND p.confirmed_at < :to"
                + scope + " GROUP BY p.supplier_id");
        for (Query q : new Query[] {ordered, delivered}) {
            q.setParameter("from", from).setParameter("to", to);
            if (locationIds != null) q.setParameter("ids", locationIds);
            if (supplierId != null) q.setParameter("supplierId", supplierId);
        }
        Map<UUID, long[]> bySupplier = new TreeMap<>();
        for (Object[] r : (List<Object[]>) ordered.getResultList()) {
            long[] v = bySupplier.computeIfAbsent((UUID) r[0], k -> new long[7]);
            v[0] = ((Number) r[1]).longValue();
            v[1] = ((Number) r[2]).longValue();
            v[2] = ((Number) r[3]).longValue();
        }
        for (Object[] r : (List<Object[]>) delivered.getResultList()) {
            long[] v = bySupplier.computeIfAbsent((UUID) r[0], k -> new long[7]);
            v[3] = ((Number) r[1]).longValue();
            v[4] = ((Number) r[2]).longValue();
            v[5] = ((Number) r[3]).longValue();
            v[6] = ((Number) r[4]).longValue();
        }
        return bySupplier.entrySet().stream().map(e -> {
            long[] v = e.getValue();
            return new PurchasingTotalsRow(e.getKey(), v[0], v[1], v[2], v[3], v[4], v[5], v[6]);
        }).toList();
    }
}
