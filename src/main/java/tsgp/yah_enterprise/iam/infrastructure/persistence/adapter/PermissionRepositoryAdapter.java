package tsgp.yah_enterprise.iam.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.domain.repository.PermissionRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;
import tsgp.yah_enterprise.iam.infrastructure.persistence.mapper.PermissionMapper;
import tsgp.yah_enterprise.iam.infrastructure.persistence.repository.PermissionJpaRepository;

/**
 * Implements the domain PermissionRepository port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {

    private static final Sort DEFAULT_SORT = Sort.by("permissionCode").ascending();

    private final PermissionJpaRepository jpaRepository;
    private final PermissionMapper mapper;

    @Override
    public Permission save(Permission permission) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(permission)));
    }

    @Override
    public Optional<Permission> findById(UUID permissionId) {
        return jpaRepository.findById(permissionId).map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByCode(PermissionCode permissionCode) {
        return jpaRepository.findByPermissionCode(permissionCode.value()).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll(DEFAULT_SORT).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Permission> findAll(int page, int size) {
        return jpaRepository.findAll(PageRequest.of(page, size, DEFAULT_SORT))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Permission> findAllByIds(Collection<UUID> permissionIds) {
        return jpaRepository.findAllById(permissionIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID permissionId) {
        return jpaRepository.existsById(permissionId);
    }

    @Override
    public boolean existsByCode(PermissionCode permissionCode) {
        return jpaRepository.existsByPermissionCode(permissionCode.value());
    }

    @Override
    public void deleteById(UUID permissionId) {
        jpaRepository.deleteById(permissionId);
    }
}