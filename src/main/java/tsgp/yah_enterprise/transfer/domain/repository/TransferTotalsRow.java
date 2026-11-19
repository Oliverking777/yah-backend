package tsgp.yah_enterprise.transfer.domain.repository;

import java.util.UUID;

/**
 * Per route, quantities in base units, for transfers sent in the period (cancelled ones excluded).
 * missing = sent - received - damaged, counted on transfers already received. pendingTransferCount is the
 * number of transfers SUBMITTED now, whatever the period. discrepancyCount is the number of RECEIVED
 * transfers with hasDiscrepancy set.
 */
public record TransferTotalsRow(UUID sourceLocationId, UUID destinationLocationId, long sentQuantity,
                                long receivedQuantity, long damagedQuantity, long missingQuantity,
                                long pendingTransferCount, long discrepancyCount) { }
