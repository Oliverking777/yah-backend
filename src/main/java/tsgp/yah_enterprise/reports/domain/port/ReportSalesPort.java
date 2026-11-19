package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Sales. Implemented in infrastructure/integration. */
public interface ReportSalesPort {

    List<SalesTotal> salesTotals(ReportPeriod period, LocationScope scope, SalesGroupBy groupBy);

    List<TopProduct> topProducts(ReportPeriod period, LocationScope scope, TopMetric metric, int limit);

    record SalesTotal(String key, long completedSalesCount, long grossAmount, long discountAmount) { }

    record TopProduct(UUID productId, long quantitySold, long salesValue) { }
}
