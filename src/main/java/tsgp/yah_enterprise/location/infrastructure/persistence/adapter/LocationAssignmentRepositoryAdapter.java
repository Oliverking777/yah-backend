package tsgp.yah_enterprise.location.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.location.infrastructure.persistence.mapper.LocationAssignmentMapper;
import tsgp.yah_enterprise.location.infrastructure.persistence.repository.LocationAssignmentJpaRepository;
import tsgp.yah_enterprise.location.domain.exception.UserAlreadyAssignedException;
import tsgp.yah_enterprise.location.domain.model.LocationAssignment;
import tsgp.yah_enterprise.location.domain.repository.LocationAssignmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LocationAssignmentRepositoryAdapter implements LocationAssignmentRepository {

    private final LocationAssignmentJpaRepository jpaRepository;
    private final LocationAssignmentMapper mapper;

    @Override
    public LocationAssignment save(LocationAssignment assignment) {
        try {
            // saveAndFlush so the one-location-per-user constraint is enforced immediately
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(assignment)));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyAssignedException(assignment.getUserId());
        }
    }

    @Override
    public Optional<LocationAssignment> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<LocationAssignment> findAllByLocationId(UUID locationId) {
        return jpaRepository.findAllByLocationIdOrderByAssignedAtAsc(locationId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void delete(LocationAssignment assignment) {
        jpaRepository.deleteById(assignment.getAssignmentId());
    }
}