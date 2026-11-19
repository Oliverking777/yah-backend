package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.domain.repository.TransferTotalsRow;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GetTransferTotalsUseCase {
    List<TransferTotalsRow> getTransferTotals(Instant from, Instant to, Set<UUID> locationIds, UUID sourceLocationId,
                                              UUID destinationLocationId);
}
