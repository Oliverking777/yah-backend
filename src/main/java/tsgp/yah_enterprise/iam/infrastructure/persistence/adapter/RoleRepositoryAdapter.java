package tsgp.yah_enterprise.iam.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;
import tsgp.yah_enterprise.iam.infrastructure.persistence.mapper.RoleMapper;
import tsgp.yah_enterprise.iam.infrastructure.persistence.repository.RoleJpaRepository;

/**
 * Implements the domain RoleRepository port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private static final Sort DEFAULT_SORT = Sort.by("roleName").ascending();

    private final RoleJpaRepository jpaRepository;
    private final RoleMapper mapper;

    @Override
    public Role save(Role role) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(role)));
    }

    @Override
    public Optional<Role> findById(UUID roleId) {
        return jpaRepository.findById(roleId).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(RoleName roleName) {
        return jpaRepository.findByRoleName(roleName.value()).map(mapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll(DEFAULT_SORT).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Role> findAll(int page, int size) {
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
    public boolean existsById(UUID roleId) {
        return jpaRepository.existsById(roleId);
    }

    @Override
    public boolean existsByName(RoleName roleName) {
        return jpaRepository.existsByRoleName(roleName.value());
    }

    @Override
    public boolean existsByPermissionId(UUID permissionId) {
        return jpaRepository.existsByPermissionId(permissionId);
    }

    @Override
    public void deleteById(UUID roleId) {
        jpaRepository.deleteById(roleId);
    }
}