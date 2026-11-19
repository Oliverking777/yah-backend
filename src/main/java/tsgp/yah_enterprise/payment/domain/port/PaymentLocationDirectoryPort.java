package tsgp.yah_enterprise.payment.domain.port;

import java.util.Optional;
import java.util.UUID;

/** Payment's view of the Location module. Implemented in infrastructure/integration. */
public interface PaymentLocationDirectoryPort {

    /** The one location a user is assigned to, if any. */
    Optional<UUID> assignedLocationOf(UUID userId);

    /** True only when the location exists and is ACTIVE. */
    boolean isActive(UUID locationId);

    default boolean isUserAssigned(UUID userId, UUID locationId) {
        return assignedLocationOf(userId).map(locationId::equals).orElse(false);
    }
}
