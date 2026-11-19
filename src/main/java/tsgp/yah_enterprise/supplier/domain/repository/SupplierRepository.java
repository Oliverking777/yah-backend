package tsgp.yah_enterprise.supplier.domain.repository;

import tsgp.yah_enterprise.supplier.domain.model.Supplier;
import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(UUID supplierId);
    List<Supplier> findAllByIds(Collection<UUID> supplierIds);
    Optional<Supplier> findByCode(String code);
    boolean existsByName(String name);
    List<Supplier> findAll(int page, int size, SupplierStatus status);
    List<Supplier> search(String term, int page, int size);
    long count(SupplierStatus status);
    long countSearch(String term);
}
