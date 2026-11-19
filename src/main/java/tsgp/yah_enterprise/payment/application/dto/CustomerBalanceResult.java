package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record CustomerBalanceResult(UUID customerId, String customerName, long outstandingAmount) { }
