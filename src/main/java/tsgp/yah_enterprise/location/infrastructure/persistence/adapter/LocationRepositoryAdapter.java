package tsgp.yah_enterprise.location.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.location.infrastructure.persistence.mapper.LocationMapper;
import tsgp.yah_enterprise.location.infrastructure.persistence.repository.LocationJpaRepository;
import tsgp.yah_enterprise.location.domain.exception.LocationCodeAlreadyExistsException;
import tsgp.yah_enterprise.location.domain.model.Location;
import tsgp.yah_enterprise.location.domain.model.LocationCode;
import tsgp.yah_enterprise.location.domain.model.LocationStatus;
import tsgp.yah_enterprise.location.domain.repository.LocationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryAdapter implements LocationRepository {

    private final LocationJpaRepository jpaRepository;
    private final LocationMapper mapper;

    @Override
    public Location save(Location location) {
        try {
            // saveAndFlush so a concurrent duplicate code fails here, not later at commit time
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(location)));
        } catch (DataIntegrityViolationException e) {
            throw new LocationCodeAlreadyExistsException(location.getCode().value());
        }
    }

    @Override
    public Optional<Location> findById(UUID locationId) {
        return jpaRepository.findById(locationId).map(mapper::toDomain);
    }

    @Override
    public Optional<Location> findByCode(LocationCode code) {
        return jpaRepository.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(LocationCode code) {
        return jpaRepository.existsByCode(code.value());
    }

    @Override
    public List<Location> findAll() {
        return jpaRepository.findAllByOrderByNameAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Location> findAll(int page, int size) {
        return jpaRepository.findAllByOrderByNameAsc(PageRequest.of(page, size))
                .getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Location> findAllByStatus(LocationStatus status) {
        return jpaRepository.findAllByLocationStatusOrderByNameAsc(status)
                .stream().map(mapper::toDomain).toList();
    }
}