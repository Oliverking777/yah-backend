package tsgp.yah_enterprise.iam.infrastructure.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Answers 401 (JSON) when a request needs authentication and has no valid session.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonErrorWriter errorWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object failure = request.getAttribute(SessionAuthenticationFilter.FAILURE_ATTRIBUTE);
        String message = SessionAuthenticationFilter.FAILURE_EXPIRED.equals(failure)
                ? "Your session has expired. Please log in again."
                : "Authentication required.";

        response.setHeader("WWW-Authenticate", "Bearer");
        errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, message);
    }
}