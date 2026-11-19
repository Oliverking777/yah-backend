package tsgp.yah_enterprise.iam.domain.repository;

import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Credential;

/**
 * Repository interface (port) for managing credentials.
 * The implementation lives in the infrastructure layer.
 */
public interface CredentialRepository {

    /**
     * Saves a new credential or updates an existing one.
     */
    Credential save(Credential credential);

    Optional<Credential> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}