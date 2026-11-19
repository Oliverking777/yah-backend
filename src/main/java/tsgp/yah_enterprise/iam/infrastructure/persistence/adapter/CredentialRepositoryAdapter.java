package tsgp.yah_enterprise.iam.infrastructure.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.domain.model.Credential;
import tsgp.yah_enterprise.iam.domain.repository.CredentialRepository;
import tsgp.yah_enterprise.iam.infrastructure.persistence.mapper.CredentialMapper;
import tsgp.yah_enterprise.iam.infrastructure.persistence.repository.CredentialJpaRepository;

/**
 * Implements the domain CredentialRepository port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class CredentialRepositoryAdapter implements CredentialRepository {

    private final CredentialJpaRepository jpaRepository;
    private final CredentialMapper mapper;

    @Override
    public Credential save(Credential credential) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(credential)));
    }

    @Override
    public Optional<Credential> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}