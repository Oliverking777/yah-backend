package tsgp.yah_enterprise.reports.domain.model;

import java.util.UUID;

/** One route over the period, in base units. sentQuantity: goods sent (cancelled transfers excluded). */
public record TransferReportRow(UUID sourceLocationId, String sourceLocationName, UUID destinationLocationId, String destinationLocationName, long sentQuantity, long receivedQuantity, long damagedQuantity, long missingQuantity, long pendingTransferCount, long discrepancyCount) { }
