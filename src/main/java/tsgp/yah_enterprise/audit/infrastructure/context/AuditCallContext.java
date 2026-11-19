package tsgp.yah_enterprise.audit.infrastructure.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;

import java.util.Optional;
import java.util.UUID;

/**
 * What every audit entry needs from the current request: who is acting, from which device, and the correlation id
 * that ties together everything one request did. The modules' audit adapters read it here, so none of them repeats
 * this code. Outside a web request (a bootstrap job, a test) there is no device and a fresh correlation id.
 */
public final class AuditCallContext {

    public static final String DEVICE_HEADER = "X-Device-Id";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ATTRIBUTE = AuditCallContext.class.getName() + ".correlationId";

    private AuditCallContext() { }

    /** The authenticated user, if any. */
    public static Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.ofNullable(user.userId());
        }
        return Optional.empty();
    }

    /** The X-Device-Id header of the current request, or null. */
    public static String deviceId() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader(DEVICE_HEADER);
    }

    /** The correlation id of the current request (set by AuditContextFilter), or a new one. */
    public static UUID correlationId() {
        HttpServletRequest request = currentRequest();
        if (request != null && request.getAttribute(CORRELATION_ATTRIBUTE) instanceof UUID id) {
            return id;
        }
        return UUID.randomUUID();
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }
}
