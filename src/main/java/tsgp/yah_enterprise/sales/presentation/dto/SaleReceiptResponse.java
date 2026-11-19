package tsgp.yah_enterprise.sales.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.sales.application.dto.OrderItemResult;
import tsgp.yah_enterprise.sales.application.dto.PaymentLineCommand;
import tsgp.yah_enterprise.sales.application.dto.SaleResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Everything the front end needs to build and print the receipt once the manager completes an order.
 * Data only: the layout is the client's job. Product and unit names are resolved by the client from the ids.
 */
@Schema(description = "Completed sale, shaped for the front end to build the receipt")
public record SaleReceiptResponse(
        String receiptNumber, Instant issuedAt, UUID issuedBy,
        UUID orderId, String orderNumber, String orderStatus, Instant completedAt,
        UUID customerId, String customerName, UUID locationId, String locationName,
        List<Line> lines, List<Payment> payments,
        long subtotal, long discountTotal, long totalAmount, long amountPaid, long amountDue,
        boolean fullyPaid, String note) {

    public record Line(UUID orderItemId, UUID productId, UUID unitId, int quantity, Long unitPrice,
                       long discountAmount, long lineTotal) {
        static Line from(OrderItemResult i) {
            return new Line(i.orderItemId(), i.productId(), i.unitId(), i.quantity(), i.unitPrice(),
                    i.discountAmount(), i.lineTotal());
        }
    }

    public record Payment(String paymentMethod, long amount, String reference) {
        static Payment from(PaymentLineCommand p) {
            return new Payment(p.paymentMethod().name(), p.amount(), p.reference());
        }
    }

    public SaleReceiptResponse {
        lines = List.copyOf(lines);
        payments = List.copyOf(payments);
    }

    public static SaleReceiptResponse from(SaleResult s) {
        var o = s.order();
        return new SaleReceiptResponse(s.receiptNumber(), s.receiptIssuedAt(), o.completedBy(),
                o.orderId(), o.orderNumber(), o.orderStatus(), o.completedAt(),
                o.customerId(), s.customerName(), o.locationId(), s.locationName(),
                o.items().stream().map(Line::from).toList(), s.payments().stream().map(Payment::from).toList(),
                o.subtotal(), o.discountTotal(), o.totalAmount(), s.amountPaid(), s.amountDue(),
                s.amountDue() == 0, o.note());
    }
}
