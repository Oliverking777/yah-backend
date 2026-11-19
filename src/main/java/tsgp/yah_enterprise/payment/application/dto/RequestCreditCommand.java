package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record RequestCreditCommand(UUID saleId, UUID customerId, long requestedAmount, String reason,
                                   PaymentActor actor) { }
