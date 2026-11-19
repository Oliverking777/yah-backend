package tsgp.yah_enterprise.sales.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Data only: the printable layout is the client's job. Built from the frozen order. */
public record ReceiptResult(String receiptNumber, UUID orderId, String orderNumber, UUID customerId,
                            UUID locationId, Instant issuedAt, UUID issuedBy, List<OrderItemResult> lines,
                            long subtotal, long discountTotal, long totalAmount, long amountPaid,
                            long amountDue) {

    public ReceiptResult {
        lines = List.copyOf(lines);
    }
}
