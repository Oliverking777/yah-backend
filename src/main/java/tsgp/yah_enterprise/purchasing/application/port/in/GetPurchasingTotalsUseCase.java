package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.domain.repository.PurchasingTotalsRow;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GetPurchasingTotalsUseCase {
    List<PurchasingTotalsRow> getPurchasingTotals(Instant from, Instant to, Set<UUID> locationIds, UUID supplierId);
}
