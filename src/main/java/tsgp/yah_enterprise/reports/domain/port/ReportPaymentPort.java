package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Payment. Implemented in infrastructure/integration. */
public interface ReportPaymentPort {

    List<AmountByKey> collectedTotals(ReportPeriod period, LocationScope scope, SalesGroupBy groupBy);

    /** Debt still open of the sales completed in the period, as of now. */
    List<AmountByKey> outstandingTotals(ReportPeriod period, LocationScope scope, SalesGroupBy groupBy);

    List<DebtLine> debtRows(ReportPeriod period, LocationScope scope, UUID customerId, boolean onlyOutstanding);

    record AmountByKey(String key, long amount) { }

    record DebtLine(UUID customerId, UUID locationId, long amountDue, long amountPaid) { }
}
