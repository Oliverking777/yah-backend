package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record ReversePaymentCommand(UUID paymentId, String reason, PaymentActor actor) { }
