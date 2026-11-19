package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record AllocationLine(UUID saleId, long amount) { }
