package tsgp.yah_enterprise.payment.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AllocationResult(UUID allocationId, UUID paymentId, UUID saleId, long allocatedAmount,
                               String allocationStatus, Instant createdAt, Instant reversedAt) { }
