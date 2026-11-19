package tsgp.yah_enterprise.sales.application.port.in;

import tsgp.yah_enterprise.sales.domain.repository.TopProductRow;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Best-selling products of completed orders, by quantity (base units) or by value. limit 1 to 50. */
public interface GetTopProductsUseCase {
    List<TopProductRow> getTopProducts(Instant from, Instant to, Set<UUID> locationIds, boolean byValue, int limit);
}
