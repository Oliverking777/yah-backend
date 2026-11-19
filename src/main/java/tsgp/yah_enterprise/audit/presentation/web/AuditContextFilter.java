package tsgp.yah_enterprise.audit.presentation.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tsgp.yah_enterprise.audit.infrastructure.context.AuditCallContext;

import java.io.IOException;
import java.util.UUID;

/**
 * Gives every request a correlation id: the X-Correlation-Id header when it is a valid UUID, a new one otherwise.
 * It runs before the security filters, so the security handlers can audit with it. The id is echoed in the response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        UUID correlationId = parse(request.getHeader(AuditCallContext.CORRELATION_HEADER));
        request.setAttribute(AuditCallContext.CORRELATION_ATTRIBUTE, correlationId);
        response.setHeader(AuditCallContext.CORRELATION_HEADER, correlationId.toString());
        chain.doFilter(request, response);
    }

    private static UUID parse(String header) {
        if (header != null) {
            try {
                return UUID.fromString(header.trim());
            } catch (IllegalArgumentException ignored) {
                // not a UUID: a new one is generated
            }
        }
        return UUID.randomUUID();
    }
}
