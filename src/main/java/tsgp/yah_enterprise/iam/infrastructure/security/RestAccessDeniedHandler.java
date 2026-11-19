package tsgp.yah_enterprise.iam.infrastructure.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Answers 403 (JSON) when a logged-in user lacks the permission an endpoint requires.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonErrorWriter errorWriter;
    private final SecurityAuditRecorder auditRecorder;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        auditRecorder.accessDenied(request.getMethod(), request.getRequestURI());
        errorWriter.write(request, response, HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.");
    }
}