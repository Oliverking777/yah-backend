package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface DashboardPaymentPort {

    long collectedToday(LocationScope scope, LocalDate day);

    List<LocationAmount> collectedByLocation(DashboardPeriod period, LocationScope scope);

    /** Every open debt in scope, not only today's. */
    long outstandingBalance(LocationScope scope);

    long pendingCreditCount(LocationScope scope);

    List<PendingCredit> pendingCredits(LocationScope scope, int limit);

    record LocationAmount(UUID locationId, long amount) { }

    record PendingCredit(UUID creditApprovalId, UUID saleId, UUID locationId, Instant since) { }
}
