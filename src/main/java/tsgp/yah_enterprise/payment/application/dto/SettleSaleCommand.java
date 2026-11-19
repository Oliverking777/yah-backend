package tsgp.yah_enterprise.payment.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Sent by Sales when it completes a sale, inside Sales' own transaction. */
public record SettleSaleCommand(UUID saleId, UUID customerId, UUID locationId, long totalAmount,
                                List<SettlementPayment> payments, UUID creditApprovalId, UUID userId,
                                Instant paidAt) { }
