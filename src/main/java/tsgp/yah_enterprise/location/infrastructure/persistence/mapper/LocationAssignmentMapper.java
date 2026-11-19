package tsgp.yah_enterprise.location.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.infrastructure.persistence.entity.LocationAssignmentJpaEntity;
import tsgp.yah_enterprise.location.domain.model.LocationAssignment;

@Component
public class LocationAssignmentMapper {

    public LocationAssignment toDomain(LocationAssignmentJpaEntity entity) {
        return LocationAssignment.reconstitute(
                entity.getAssignmentId(),
                entity.getLocationId(),
                entity.getUserId(),
                entity.getAssignedAt());
    }

    public LocationAssignmentJpaEntity toEntity(LocationAssignment assignment) {
        LocationAssignmentJpaEntity entity = new LocationAssignmentJpaEntity();
        entity.setAssignmentId(assignment.getAssignmentId());
        entity.setLocationId(assignment.getLocationId());
        entity.setUserId(assignment.getUserId());
        entity.setAssignedAt(assignment.getAssignedAt());
        return entity;
    }
}