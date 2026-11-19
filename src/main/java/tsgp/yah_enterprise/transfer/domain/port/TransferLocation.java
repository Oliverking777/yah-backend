package tsgp.yah_enterprise.transfer.domain.port;

import java.util.UUID;

public record TransferLocation(UUID locationId, String locationName, boolean active) { }
