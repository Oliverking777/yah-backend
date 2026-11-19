package tsgp.yah_enterprise.product.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.domain.model.Product;
import tsgp.yah_enterprise.product.domain.model.ProductVariant;
import tsgp.yah_enterprise.product.domain.model.VariantUnit;
import tsgp.yah_enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import tsgp.yah_enterprise.product.infrastructure.persistence.entity.ProductVariantJpaEntity;
import tsgp.yah_enterprise.product.infrastructure.persistence.entity.VariantUnitJpaEntity;

/**
 * Converts a whole Product aggregate (variants and their units included) to and from its entities.
 * toEntity builds a detached graph with the domain ids; saving merges it, so children removed in the domain are
 * deleted (orphanRemoval) and new ones inserted.
 */
@Component
public class ProductMapper {

    public Product toDomain(ProductJpaEntity e) {
        return Product.reconstitute(e.getProductId(), e.getProductCode(), e.getProductName(),
                e.getDescription(), e.getProductImageUrl(), e.getCategoryId(), e.getProductStatus(),
                e.getVariants().stream().map(this::variantToDomain).toList(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity e = new ProductJpaEntity();
        e.setProductId(product.getProductId());
        e.setProductCode(product.getProductCode().value());
        e.setProductName(product.getProductName().value());
        e.setDescription(product.getDescription());
        e.setProductImageUrl(product.getProductImageUrl().value());
        e.setCategoryId(product.getCategoryId());
        e.setProductStatus(product.getProductStatus());
        e.setCreatedAt(product.getCreatedAt());
        e.setUpdatedAt(product.getUpdatedAt());
        for (ProductVariant variant : product.getVariants()) {
            e.getVariants().add(variantToEntity(variant, e));
        }
        return e;
    }

    private ProductVariant variantToDomain(ProductVariantJpaEntity v) {
        return ProductVariant.reconstitute(v.getVariantId(), v.getSku(), v.getVariantName(),
                v.getVariantStatus(), v.getUnits().stream().map(this::unitToDomain).toList(),
                v.getCreatedAt(), v.getUpdatedAt());
    }

    private ProductVariantJpaEntity variantToEntity(ProductVariant variant, ProductJpaEntity product) {
        ProductVariantJpaEntity v = new ProductVariantJpaEntity();
        v.setVariantId(variant.getVariantId());
        v.setProduct(product);
        v.setVariantName(variant.getVariantName() == null ? null : variant.getVariantName().value());
        v.setNameKey(variant.nameKey());
        v.setSku(variant.getSku().value());
        v.setVariantStatus(variant.getVariantStatus());
        v.setCreatedAt(variant.getCreatedAt());
        v.setUpdatedAt(variant.getUpdatedAt());
        for (VariantUnit unit : variant.getUnits()) {
            VariantUnitJpaEntity u = new VariantUnitJpaEntity();
            u.setVariantUnitId(unit.getVariantUnitId());
            u.setVariant(v);
            u.setUnitId(unit.getUnitId());
            u.setBaseUnit(unit.isBaseUnit());
            u.setConversionFactor(unit.getConversionFactor());
            u.setUnitStatus(unit.getUnitStatus());
            u.setCreatedAt(unit.getCreatedAt());
            u.setUpdatedAt(unit.getUpdatedAt());
            v.getUnits().add(u);
        }
        return v;
    }

    private VariantUnit unitToDomain(VariantUnitJpaEntity u) {
        return VariantUnit.reconstitute(u.getVariantUnitId(), u.getUnitId(), u.isBaseUnit(), u.getConversionFactor(),
                u.getUnitStatus(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
