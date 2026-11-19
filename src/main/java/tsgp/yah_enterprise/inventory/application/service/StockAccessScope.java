package tsgp.yah_enterprise.inventory.application.service;

import tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Applies the role's location scope (ALL or ASSIGNED). A user limited to ASSIGNED only sees and acts on
 * the location they are assigned to. The user id always comes from the session, never from the request body.
 */
@Component
class StockAccessScope {

    private final InventoryLocationDirectoryPort locationDirectory;

    StockAccessScope(InventoryLocationDirectoryPort locationDirectory) {
        this.locationDirectory = locationDirectory;
    }

    /**
     * The location a listing must be limited to: the requested one for an unrestricted user (may be null = all),
     * the user's own location for a restricted one.
     */
    UUID effectiveLocation(UUID requestedLocationId, UUID userId, boolean restricted) {
        if (!restricted) {
            return requestedLocationId;
        }
        UUID own = locationDirectory.findLocationOfUser(userId).orElseThrow(() ->
                new StockLocationAccessException("You are not assigned to any location"));
        if (requestedLocationId != null && !requestedLocationId.equals(own)) {
            throw new StockLocationAccessException("You can only access your own location");
        }
        return own;
    }

    /** Refuses a restricted user acting on a location other than their own. */
    void checkCanAct(UUID locationId, UUID userId, boolean restricted) {
        if (restricted) {
            effectiveLocation(locationId, userId, true);
        }
    }
}
