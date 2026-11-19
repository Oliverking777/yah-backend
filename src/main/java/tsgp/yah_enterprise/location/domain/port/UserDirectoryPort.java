package tsgp.yah_enterprise.location.domain.port;

import tsgp.yah_enterprise.location.domain.model.LocationUser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: how the Location module asks "who is this user?".
 * Implemented by an adapter that calls IAM's inbound port; Location never touches
 * IAM's User entity or UserRepository.
 */
public interface UserDirectoryPort {
    Optional<LocationUser> findById(UUID userId);

    /** Batch lookup used when listing a location's users (avoids N+1 calls). */
    List<LocationUser> findAllByIds(Collection<UUID> userIds);
}