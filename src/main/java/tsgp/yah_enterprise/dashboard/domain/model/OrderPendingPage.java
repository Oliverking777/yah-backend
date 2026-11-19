package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPendingPage(List<Item> items, int page, int size, long totalElements, int totalPages) {

    public record Item(UUID orderId, String orderNumber, UUID locationId, String locationName, UUID customerId, long totalAmount, Instant createdAt) { }
}
