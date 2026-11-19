package tsgp.yah_enterprise.payment.application.dto;

import java.util.List;
import java.util.UUID;

/** lines null or empty means: spread over the customer's debts, oldest first. */
public record AllocatePaymentCommand(UUID paymentId, List<AllocationLine> lines, PaymentActor actor) { }
