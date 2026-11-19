package tsgp.yah_enterprise.audit.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Reading someone's activity is itself audited (AUDIT.USER_ACTIVITY_VIEWED), so the viewer is part of the request. */
public record UserActivityRequest(UUID userId, Instant from, Instant to, int page, int size,
                                  AuditRequestContext context) {

    public UserActivityRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
