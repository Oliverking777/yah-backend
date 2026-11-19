package tsgp.yah_enterprise.payment.application.dto;

import java.time.Instant;
import java.util.UUID;

public record DebtResult(UUID saleId, UUID customerId, UUID locationId, long totalAmount, long allocatedAmount,
                         long outstandingAmount, String receivableStatus, Instant createdAt) { }
