package tsgp.yah_enterprise.transfer.application.dto;

import java.util.UUID;

public record ReceiveLineCommand(UUID productId, int receivedQuantity, int damagedQuantity,
                                 String discrepancyReason) { }
