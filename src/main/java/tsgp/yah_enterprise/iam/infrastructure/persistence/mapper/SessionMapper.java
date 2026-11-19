package tsgp.yah_enterprise.iam.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.domain.model.ClientType;
import tsgp.yah_enterprise.iam.domain.model.Session;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionTokenHash;
import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.SessionJpaEntity;

/**
 * Converts between the domain Session and the SessionJpaEntity.
 */
@Component
public class SessionMapper {

    public SessionJpaEntity toEntity(Session session) {
        return new SessionJpaEntity(
                session.getSessionId(),
                session.getUserId(),
                session.getTokenHash().value(),
                session.getClientType().name(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getIpAddress(),
                session.getUserAgent());
    }

    public Session toDomain(SessionJpaEntity entity) {
        return Session.reconstructSession(
                entity.getSessionId(),
                entity.getUserId(),
                SessionTokenHash.of(entity.getTokenHash()),
                ClientType.valueOf(entity.getClientType()),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getIpAddress(),
                entity.getUserAgent());
    }
}