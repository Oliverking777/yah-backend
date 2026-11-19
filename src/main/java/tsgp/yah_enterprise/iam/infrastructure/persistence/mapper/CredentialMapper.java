package tsgp.yah_enterprise.iam.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import tsgp.yah_enterprise.iam.domain.model.Credential;
import tsgp.yah_enterprise.iam.domain.valueobject.PasswordHash;
import tsgp.yah_enterprise.iam.infrastructure.persistence.entity.CredentialJpaEntity;

/**
 * Converts between the domain Credential and the CredentialJpaEntity.
 */
@Component
public class CredentialMapper {

    public CredentialJpaEntity toEntity(Credential credential) {
        return new CredentialJpaEntity(
                credential.getCredentialId(),
                credential.getUserId(),
                credential.getPasswordHash().value(),
                credential.getCreatedAt(),
                credential.getPasswordChangedAt());
    }

    public Credential toDomain(CredentialJpaEntity entity) {
        return Credential.reconstructCredential(
                entity.getCredentialId(),
                entity.getUserId(),
                PasswordHash.of(entity.getPasswordHash()),
                entity.getCreatedAt(),
                entity.getPasswordChangedAt());
    }
}