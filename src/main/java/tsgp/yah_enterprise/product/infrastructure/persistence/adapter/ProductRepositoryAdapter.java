package tsgp.yah_enterprise.product.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.product.domain.exception.DuplicateVariantNameException;
import tsgp.yah_enterprise.product.domain.exception.ProductCodeAlreadyExistsException;
import tsgp.yah_enterprise.product.domain.exception.VariantSkuAlreadyExistsException;
import tsgp.yah_enterprise.product.domain.model.Product;
import tsgp.yah_enterprise.product.domain.model.ProductStatus;
import tsgp.yah_enterprise.product.domain.model.ProductVariant;
import tsgp.yah_enterprise.product.domain.repository.ProductRepository;
import tsgp.yah_enterprise.product.infrastructure.persistence.mapper.ProductMapper;
import tsgp.yah_enterprise.product.infrastructure.persistence.repository.ProductJpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    static final String SKU_CONSTRAINT = "uq_product_variants_sku";
    static final String VARIANT_NAME_CONSTRAINT = "uq_product_variants_name";

    private final ProductJpaRepository jpa;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        try {
            // saveAndFlush so a concurrent duplicate fails here, not later at commit time
            return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(product)));
        } catch (DataIntegrityViolationException e) {
            // The service checks codes and SKUs first; this only happens when two requests race.
            // No query can run after a failed statement (PostgreSQL aborts the transaction), so the violated
            // constraint's name tells which rule broke. The newest variant is the one being added or changed.
            ProductVariant newest = product.getVariants().get(product.getVariants().size() - 1);
            if (violated(e, SKU_CONSTRAINT)) {
                throw new VariantSkuAlreadyExistsException(newest.getSku().value());
            }
            if (violated(e, VARIANT_NAME_CONSTRAINT)) {
                throw new DuplicateVariantNameException(
                        newest.getVariantName() == null ? "" : newest.getVariantName().value());
            }
            throw new ProductCodeAlreadyExistsException(product.getProductCode().value());
        }
    }

    static boolean violated(Throwable e, String constraint) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains(constraint)) {
                return true;
            }
            cause = cause.getCause() == cause ? null : cause.getCause();
        }
        return false;
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return jpa.findById(productId).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return jpa.findByProductCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByProductCodeIgnoreCase(code);
    }

    @Override
    public Optional<Product> findByVariantId(UUID variantId) {
        return jpa.findByVariantId(variantId).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllByVariantIds(Collection<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        return jpa.findAllByVariantIds(variantIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpa.existsBySku(sku.trim().toUpperCase());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            return Optional.empty();
        }
        return jpa.findBySku(sku.trim()).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAll(int page, int size, ProductStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("productName").ascending());
        if (status != null) {
            return jpa.findAllByProductStatus(status, pageable).getContent()
                    .stream().map(mapper::toDomain).toList();
        }
        return jpa.findAll(pageable).getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Product> search(String term, int page, int size) {
        return jpa.search(term, PageRequest.of(page, size)).getContent()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(ProductStatus status) {
        return status != null ? jpa.countByProductStatus(status) : jpa.count();
    }

    @Override
    public long countSearch(String term) {
        return jpa.search(term, PageRequest.of(0, 1)).getTotalElements();
    }
}
