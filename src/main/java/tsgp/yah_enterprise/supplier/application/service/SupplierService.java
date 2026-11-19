package tsgp.yah_enterprise.supplier.application.service;

import tsgp.yah_enterprise.supplier.domain.port.SupplierAuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.supplier.application.dto.*;
import tsgp.yah_enterprise.supplier.application.port.in.*;
import tsgp.yah_enterprise.supplier.domain.exception.SupplierNameAlreadyExistsException;
import tsgp.yah_enterprise.supplier.domain.exception.SupplierNotFoundException;
import tsgp.yah_enterprise.supplier.domain.model.Supplier;
import tsgp.yah_enterprise.supplier.domain.repository.SupplierRepository;
import tsgp.yah_enterprise.supplier.domain.valueobject.SupplierCode;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService implements
        CreateSupplierUseCase, GetSupplierUseCase, GetSuppliersByIdsUseCase,
        ListSuppliersUseCase, UpdateSupplierUseCase, ChangeSupplierStatusUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierAuditPort audit;
    private final SupplierCodeSequence supplierCodeSequence;

    @Override
    public SupplierResult createSupplier(CreateSupplierCommand command) {
        if (supplierRepository.existsByName(command.supplierName())) {
            throw new SupplierNameAlreadyExistsException(command.supplierName());
        }
        SupplierCode code = SupplierCode.fromSequence(supplierCodeSequence.next());
        Supplier supplier = Supplier.create(code, command.supplierName(), command.contactPerson(),
                command.phoneNumber(), command.email(), command.address());
        return SupplierResult.from(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResult getSupplier(UUID supplierId) {
        return SupplierResult.from(find(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResult> getSuppliersByIds(Collection<UUID> supplierIds) {
        return supplierRepository.findAllByIds(supplierIds).stream()
                .map(SupplierResult::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPageResult listSuppliers(SupplierPageRequest request) {
        boolean searching = request.search() != null && !request.search().isBlank();
        List<SupplierResult> content = searching
                ? supplierRepository.search(request.search(), request.page(), request.size())
                        .stream().map(SupplierResult::from).toList()
                : supplierRepository.findAll(request.page(), request.size(), request.status())
                        .stream().map(SupplierResult::from).toList();
        long total = searching
                ? supplierRepository.countSearch(request.search())
                : supplierRepository.count(request.status());
        return SupplierPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public SupplierResult updateSupplier(UUID supplierId, UpdateSupplierCommand command) {
        Supplier supplier = find(supplierId);
        if (command.supplierName() != null) {
            boolean usedByAnother = supplierRepository.existsByName(command.supplierName()) &&
                    supplierRepository.findByCode(supplier.getSupplierCode().value())
                            .map(s -> !s.getSupplierName().value().equalsIgnoreCase(command.supplierName()))
                            .orElse(false);
            if (usedByAnother) throw new SupplierNameAlreadyExistsException(command.supplierName());
            supplier.rename(command.supplierName());
        }
        supplier.changeContact(command.contactPerson(), command.phoneNumber(),
                command.email(), command.address());
        return SupplierResult.from(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResult activateSupplier(UUID supplierId) {
        Supplier supplier = find(supplierId);
        supplier.activate();
        Supplier saved = supplierRepository.save(supplier);
        audit.record(new SupplierAuditPort.AuditRecord(null, "SUPPLIER.STATUS_CHANGED", "SUPPLIER", supplierId, null, null,
                List.of(new SupplierAuditPort.Change("supplierStatus", "INACTIVE", "ACTIVE"))));
        return SupplierResult.from(saved);
    }

    @Override
    public SupplierResult deactivateSupplier(UUID supplierId) {
        Supplier supplier = find(supplierId);
        supplier.deactivate();
        Supplier saved = supplierRepository.save(supplier);
        audit.record(new SupplierAuditPort.AuditRecord(null, "SUPPLIER.STATUS_CHANGED", "SUPPLIER", supplierId, null, null,
                List.of(new SupplierAuditPort.Change("supplierStatus", "ACTIVE", "INACTIVE"))));
        return SupplierResult.from(saved);
    }

    private Supplier find(UUID supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    }
}
