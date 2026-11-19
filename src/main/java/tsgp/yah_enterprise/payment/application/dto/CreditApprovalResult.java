package tsgp.yah_enterprise.payment.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CreditApprovalResult(UUID creditApprovalId, UUID saleId, UUID customerId, long requestedAmount,
                                   Long approvedAmount, String reason, String decisionNote, String approvalStatus,
                                   UUID requestedBy, UUID approvedBy, Instant requestedAt, Instant decidedAt,
                                   Instant consumedAt) { }
