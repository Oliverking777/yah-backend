package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Inventory. Implemented in infrastructure/integration. */
public interface ReportStockPort {

    StockBalancePage balances(LocationScope scope, UUID productId, StockStatus status, int page, int size);

    record StockBalance(UUID productId, UUID locationId, long quantity, Long lowStockThreshold) { }

    record StockBalancePage(List<StockBalance> items, long total) { }
}
