package tsgp.yah_enterprise.pricing.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;
import tsgp.yah_enterprise.pricing.infrastructure.persistence.entity.ProductPriceJpaEntity;

@Component
public class ProductPriceMapper {

    public ProductPrice toDomain(ProductPriceJpaEntity entity) {
        return ProductPrice.reconstitute(
                entity.getPriceId(), entity.getProductId(), entity.getUnitId(),
                entity.getPrice(), entity.getMinimumQuantity(), entity.getMaximumQuantity(),
                entity.getEffectiveFrom(), entity.getEffectiveTo(), entity.getPriceStatus(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public ProductPriceJpaEntity toEntity(ProductPrice price) {
        ProductPriceJpaEntity entity = new ProductPriceJpaEntity();
        entity.setPriceId(price.getPriceId());
        entity.setProductId(price.getProductId());
        entity.setUnitId(price.getUnitId());
        entity.setPrice(price.getPrice().amount());                      // unwrap the value objects
        entity.setMinimumQuantity(price.getQuantityRange().min());
        entity.setMaximumQuantity(price.getQuantityRange().max());
        entity.setEffectiveFrom(price.getEffectiveFrom());
        entity.setEffectiveTo(price.getEffectiveTo());
        entity.setPriceStatus(price.getPriceStatus());
        entity.setCreatedAt(price.getCreatedAt());
        entity.setUpdatedAt(price.getUpdatedAt());
        return entity;
    }
}
