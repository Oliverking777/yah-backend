package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface DashboardSalesPort {

    SalesToday completedToday(LocationScope scope, LocalDate day);

    List<LocationSales> salesByLocation(DashboardPeriod period, LocationScope scope);

    long pendingOrderCount(LocationScope scope);

    PendingOrderPage pendingOrders(LocationScope scope, int page, int size);

    record SalesToday(long completedSalesCount, long netAmount) { }

    record LocationSales(UUID locationId, long completedSalesCount, long netAmount) { }

    record PendingOrder(UUID orderId, String orderNumber, UUID locationId, UUID customerId, long totalAmount, Instant createdAt) { }

    record PendingOrderPage(List<PendingOrder> items, long total) { }
}
