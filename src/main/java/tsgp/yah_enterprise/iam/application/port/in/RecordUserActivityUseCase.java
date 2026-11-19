package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

/**
 * Records that a user was just active (made an authenticated request).
 * Implementations may skip the write when the stored value is recent enough.
 */
public interface RecordUserActivityUseCase {

    void recordActivity(UUID userId);
}