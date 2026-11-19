package tsgp.yah_enterprise.inventory.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.inventory.domain.repository.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockReportingRepositoryAdapter implements StockReportingRepository {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public StockBalancePage balances(Set<UUID> locationIds, UUID productId, String status, int page, int size) {
        String inner = "SELECT s.product_id AS product_id, s.location_id AS location_id, s.quantity AS quantity, "
                + "s.low_stock_threshold AS threshold, CASE WHEN s.quantity <= 0 THEN 'OUT_OF_STOCK' "
                + "WHEN s.low_stock_threshold IS NOT NULL AND s.quantity <= s.low_stock_threshold THEN 'LOW' "
                + "ELSE 'OK' END AS status FROM stock_levels s WHERE 1 = 1"
                + (locationIds == null ? "" : " AND s.location_id IN (:ids)")
                + (productId == null ? "" : " AND s.product_id = :productId");
        String filter = status == null ? "" : " WHERE t.status = :status";
        Query count = em.createNativeQuery("SELECT COUNT(*) FROM (" + inner + ") t" + filter);
        Query rows = em.createNativeQuery("SELECT t.product_id, t.location_id, t.quantity, t.threshold, t.status FROM ("
                + inner + ") t" + filter + " ORDER BY t.location_id, t.product_id LIMIT :size OFFSET :offset");
        for (Query q : new Query[] {count, rows}) {
            if (locationIds != null) q.setParameter("ids", locationIds);
            if (productId != null) q.setParameter("productId", productId);
            if (status != null) q.setParameter("status", status);
        }
        rows.setParameter("size", size).setParameter("offset", (long) page * size);
        long total = ((Number) count.getSingleResult()).longValue();
        List<StockBalanceRow> items = ((List<Object[]>) rows.getResultList()).stream()
                .map(r -> new StockBalanceRow((UUID) r[0], (UUID) r[1], ((Number) r[2]).longValue(),
                        r[3] == null ? null : ((Number) r[3]).longValue(), String.valueOf(r[4]))).toList();
        return new StockBalancePage(items, total);
    }
}
