package tsgp.yah_enterprise.supplier.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.supplier.domain.exception.SupplierNameAlreadyExistsException;
import tsgp.yah_enterprise.supplier.domain.model.Supplier;
import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;
import tsgp.yah_enterprise.supplier.domain.repository.SupplierRepository;
import tsgp.yah_enterprise.supplier.infrastructure.persistence.mapper.SupplierMapper;
import tsgp.yah_enterprise.supplier.infrastructure.persistence.repository.SupplierJpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements SupplierRepository {

    private final SupplierJpaRepository jpa;
    private final SupplierMapper mapper;

    @Override
    public Supplier save(Supplier supplier) {
        try {
            return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(supplier)));
        } catch (DataIntegrityViolationException e) {
            throw new SupplierNameAlreadyExistsException(supplier.getSupplierName().value());
        }
    }

    @Override
    public Optional<Supplier> findById(UUID supplierId) {
        return jpa.findById(supplierId).map(mapper::toDomain);
    }

    @Override
    public List<Supplier> findAllByIds(Collection<UUID> supplierIds) {
        return jpa.findAllBySupplierIdIn(supplierIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Supplier> findByCode(String code) {
        return jpa.findBySupplierCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsBySupplierNameIgnoreCase(name);
    }

    @Override
    public List<Supplier> findAll(int page, int size, SupplierStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("supplierName").ascending());
        if (status != null) {
            return jpa.findAllBySupplierStatus(status, pageable).getContent()
                    .stream().map(mapper::toDomain).toList();
        }
        return jpa.findAll(pageable).getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Supplier> search(String term, int page, int size) {
        return jpa.search(term, PageRequest.of(page, size)).getContent()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(SupplierStatus status) {
        return status != null ? jpa.countBySupplierStatus(status) : jpa.count();
    }

    @Override
    public long countSearch(String term) {
        return jpa.search(term, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
    }
}
