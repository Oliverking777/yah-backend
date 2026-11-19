package tsgp.yah_enterprise.payment.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentResult(UUID paymentId, String paymentNumber, UUID customerId, UUID locationId, long amount,
                            String paymentMethod, String reference, UUID receivedBy, Instant receivedAt,
                            String paymentStatus, long allocatedAmount, long unallocatedAmount, UUID reversedBy,
                            String reversalReason, Instant reversedAt, Instant createdAt, Instant updatedAt) { }
