package tsgp.yah_enterprise.location.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.infrastructure.persistence.entity.LocationJpaEntity;
import tsgp.yah_enterprise.location.domain.model.Location;
import tsgp.yah_enterprise.location.domain.model.LocationCode;

@Component
public class LocationMapper {

    public Location toDomain(LocationJpaEntity entity) {
        return Location.reconstitute(
                entity.getLocationId(),
                entity.getName(),
                new LocationCode(entity.getCode()),
                entity.getLocationType(),
                entity.getAddress(),
                entity.getLocationStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public LocationJpaEntity toEntity(Location location) {
        LocationJpaEntity entity = new LocationJpaEntity();
        entity.setLocationId(location.getLocationId());
        entity.setName(location.getName());
        entity.setCode(location.getCode().value());
        entity.setLocationType(location.getLocationType());
        entity.setAddress(location.getAddress());
        entity.setLocationStatus(location.getLocationStatus());
        entity.setCreatedAt(location.getCreatedAt());
        entity.setUpdatedAt(location.getUpdatedAt());
        return entity;
    }
}