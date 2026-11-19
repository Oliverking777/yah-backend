package tsgp.yah_enterprise.customer.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.customer.domain.exception.*;

@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.customer.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomerExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(CustomerNotFoundException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            CustomerAlreadyActiveException.class,
            CustomerAlreadyInactiveException.class,
            CustomerInactiveException.class,
            WalkInCustomerProtectedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidCustomerException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidCustomerException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, req.getRequestURI()));
    }
}
