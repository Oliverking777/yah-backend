package tsgp.yah_enterprise.iam.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.domain.model.User;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;
import tsgp.yah_enterprise.iam.infrastructure.persistence.mapper.UserMapper;
import tsgp.yah_enterprise.iam.infrastructure.persistence.repository.UserJpaRepository;

/**
 * Implements the domain UserRepository port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private static final Sort DEFAULT_SORT = Sort.by("userName").ascending();

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaRepository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUserName(Username userName) {
        return jpaRepository.findByUserName(userName.value()).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll(int page, int size) {
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
    public boolean existsById(UUID userId) {
        return jpaRepository.existsById(userId);
    }

    @Override
    public boolean existsByUserName(Username userName) {
        return jpaRepository.existsByUserName(userName.value());
    }

    @Override
    public boolean existsByRoleId(UUID roleId) {
        return jpaRepository.existsByRoleId(roleId);
    }

    @Override
    public boolean updateLastActiveAt(UUID userId, Instant activeAt, Instant staleBefore) {
        return jpaRepository.updateLastActiveAt(userId, activeAt, staleBefore) > 0;
    }

    @Override
    public void deleteById(UUID userId) {
        jpaRepository.deleteById(userId);
    }
}