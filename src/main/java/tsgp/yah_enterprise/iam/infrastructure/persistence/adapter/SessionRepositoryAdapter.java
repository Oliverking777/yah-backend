package tsgp.yah_enterprise.iam.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.domain.model.Session;
import tsgp.yah_enterprise.iam.domain.repository.SessionRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionTokenHash;
import tsgp.yah_enterprise.iam.infrastructure.persistence.mapper.SessionMapper;
import tsgp.yah_enterprise.iam.infrastructure.persistence.repository.SessionJpaRepository;

/**
 * Implements the domain SessionRepository port using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class SessionRepositoryAdapter implements SessionRepository {

    private final SessionJpaRepository jpaRepository;
    private final SessionMapper mapper;

    @Override
    public Session save(Session session) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<Session> findByTokenHash(SessionTokenHash tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Session> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public int deleteExpired(Instant now) {
        return jpaRepository.deleteExpired(now);
    }
}