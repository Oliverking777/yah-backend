package tsgp.yah_enterprise.dashboard.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.dashboard.domain.exception.*;

/** Maps the Dashboard exceptions to HTTP. Errors are never cached either. */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.dashboard.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DashboardExceptionHandler {

    @ExceptionHandler(InvalidDashboardRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(DashboardLocationAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(DashboardSourceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailable(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
