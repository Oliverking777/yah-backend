package tsgp.yah_enterprise.transfer.application.port.in;

import tsgp.yah_enterprise.transfer.domain.repository.InTransitRow;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GetInTransitStockUseCase {
    List<InTransitRow> getInTransitStock(Set<UUID> destinationLocationIds);
}
