package tsgp.yah_enterprise.sales.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResult(UUID orderId, String orderNumber, UUID locationId, UUID customerId, String orderStatus,
                          List<OrderItemResult> items, long subtotal, long discountTotal, long totalAmount,
                          Long amountPaid, Long amountDue, String note, UUID createdBy, UUID validatedBy,
                          UUID completedBy, UUID cancelledBy, String rejectionReason, String cancelReason,
                          Instant createdAt, Instant submittedAt, Instant validatedAt, Instant completedAt,
                          Instant updatedAt) {

    public OrderResult {
        items = List.copyOf(items);
    }
}
