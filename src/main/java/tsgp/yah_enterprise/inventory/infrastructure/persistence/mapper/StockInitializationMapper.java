package tsgp.yah_enterprise.inventory.infrastructure.persistence.mapper;

import tsgp.yah_enterprise.inventory.domain.model.StockInitialization;
import tsgp.yah_enterprise.inventory.domain.model.StockInitializationItem;
import tsgp.yah_enterprise.inventory.domain.valueobject.InitializationNumber;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockInitializationItemJpaEntity;
import tsgp.yah_enterprise.inventory.infrastructure.persistence.entity.StockInitializationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Entity <-> domain for the StockInitialization aggregate (root + child items). */
@Component
public class StockInitializationMapper {

    public StockInitialization toDomain(StockInitializationJpaEntity e) {
        List<StockInitializationItem> items = new ArrayList<>();
        if (e.getItems() != null) {
            for (StockInitializationItemJpaEntity ie : e.getItems()) {
                items.add(StockInitializationItem.reconstitute(ie.getItemId(), ie.getProductId(), ie.getUnitId(),
                        ie.getConversionFactor(), ie.getCountedQuantity(), ie.getBaseQuantity()));
            }
        }
        return StockInitialization.reconstitute(e.getInitializationId(),
                InitializationNumber.of(e.getInitializationNumber()), e.getLocationId(), e.getStatus(),
                e.getInitializedBy(), e.getCreatedAt(), e.getCompletedAt(), items);
    }

    public StockInitializationJpaEntity toEntity(StockInitialization init) {
        StockInitializationJpaEntity e = new StockInitializationJpaEntity();
        e.setInitializationId(init.getInitializationId());
        e.setInitializationNumber(init.getInitializationNumber().value());
        e.setLocationId(init.getLocationId());
        e.setStatus(init.getStatus());
        e.setInitializedBy(init.getInitializedBy());
        e.setCreatedAt(init.getCreatedAt());
        e.setCompletedAt(init.getCompletedAt());
        for (StockInitializationItem item : init.getItems()) {
            StockInitializationItemJpaEntity ie = new StockInitializationItemJpaEntity();
            ie.setItemId(item.getItemId());
            ie.setProductId(item.getProductId());
            ie.setLocationId(init.getLocationId());
            ie.setUnitId(item.getUnitId());
            ie.setConversionFactor(item.getConversionFactor());
            ie.setCountedQuantity(item.getCountedQuantity());
            ie.setBaseQuantity(item.getBaseQuantity());
            ie.setInitialization(e);
            e.getItems().add(ie);
        }
        return e;
    }
}
