package tsgp.yah_enterprise.audit.application.dto;

import java.util.UUID;

public record EntityHistoryRequest(String entityType, UUID entityId, int page, int size) {

    public EntityHistoryRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
