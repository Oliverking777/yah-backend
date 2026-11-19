package tsgp.yah_enterprise.sales.application.dto;

import java.time.Instant;
import java.util.List;

/** The completed order, its receipt number, what was paid and left on credit, and the data the receipt needs. */
public record SaleResult(OrderResult order, String receiptNumber, Instant receiptIssuedAt, long amountPaid,
                         long amountDue, String customerName, String locationName,
                         List<PaymentLineCommand> payments) {

    public SaleResult {
        payments = payments == null ? List.of() : List.copyOf(payments);
    }
}
