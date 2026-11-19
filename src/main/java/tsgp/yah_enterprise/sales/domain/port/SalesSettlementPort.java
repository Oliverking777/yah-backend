package tsgp.yah_enterprise.sales.domain.port;

import tsgp.yah_enterprise.sales.domain.model.SalesPaymentMethod;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Sales' view of the Payment module. The adapter calls Payment's SettleSaleUseCase in the same transaction. */
public interface SalesSettlementPort {

    /** @throws tsgp.yah_enterprise.sales.domain.exception.SaleSettlementRejectedException when payment refuses */
    SaleSettlement settle(SettlementRequest request);

    record SettlementRequest(UUID saleId, UUID customerId, UUID locationId, long totalAmount,
                             List<PaymentLine> payments, UUID creditApprovalId, UUID userId, Instant paidAt) { }

    record PaymentLine(SalesPaymentMethod paymentMethod, long amount, String reference) { }

    record SaleSettlement(long amountPaid, long amountDue) { }
}
