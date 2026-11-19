package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record RejectCreditCommand(UUID creditApprovalId, String note, PaymentActor actor) { }
