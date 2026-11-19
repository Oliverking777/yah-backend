package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

/** approvedAmount null means the requested amount. */
public record ApproveCreditCommand(UUID creditApprovalId, Long approvedAmount, String note, PaymentActor actor) { }
