package tsgp.yah_enterprise.purchasing.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseSupplierDirectoryPort;
import tsgp.yah_enterprise.supplier.application.dto.SupplierResult;
import tsgp.yah_enterprise.supplier.application.port.in.GetSupplierUseCase;
import tsgp.yah_enterprise.supplier.application.port.in.GetSuppliersByIdsUseCase;
import tsgp.yah_enterprise.supplier.domain.exception.SupplierNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Purchasing reads the Supplier module through its public use cases only. */
@Component
@RequiredArgsConstructor
public class PurchaseSupplierDirectoryAdapter implements PurchaseSupplierDirectoryPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetSupplierUseCase getSupplierUseCase;
    private final GetSuppliersByIdsUseCase getSuppliersByIdsUseCase;

    @Override
    public Optional<PurchaseSupplier> findById(UUID supplierId) {
        try {
            return Optional.of(toPort(getSupplierUseCase.getSupplier(supplierId)));
        } catch (SupplierNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PurchaseSupplier> findAllByIds(Collection<UUID> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return List.of();
        }
        return getSuppliersByIdsUseCase.getSuppliersByIds(supplierIds).stream().map(this::toPort).toList();
    }

    private PurchaseSupplier toPort(SupplierResult s) {
        return new PurchaseSupplier(s.supplierId(), s.supplierName(), ACTIVE.equals(s.supplierStatus()));
    }
}
