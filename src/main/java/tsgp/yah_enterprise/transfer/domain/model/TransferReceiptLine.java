package tsgp.yah_enterprise.transfer.domain.model;

import java.util.UUID;

/** What the receiver reports for one product: good quantity, damaged quantity and why they differ. */
public record TransferReceiptLine(UUID productId, int receivedQuantity, int damagedQuantity,
                                  String discrepancyReason) { }
