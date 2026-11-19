package tsgp.yah_enterprise.location.presentation.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.location.domain.exception.InvalidLocationException;
import tsgp.yah_enterprise.location.domain.exception.InvalidLocationTypeException;
import tsgp.yah_enterprise.location.domain.exception.LocationAlreadyActiveException;
import tsgp.yah_enterprise.location.domain.exception.LocationAlreadyInactiveException;
import tsgp.yah_enterprise.location.domain.exception.LocationCodeAlreadyExistsException;
import tsgp.yah_enterprise.location.domain.exception.LocationInactiveException;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;
import tsgp.yah_enterprise.location.domain.exception.LocationUserNotFoundException;
import tsgp.yah_enterprise.location.domain.exception.UserAlreadyAssignedException;
import tsgp.yah_enterprise.location.domain.exception.UserNotAssignableException;
import tsgp.yah_enterprise.location.domain.exception.UserNotAssignedException;

/**
 * Turns Location domain exceptions into ApiResponse bodies.
 * <p>
 * It only handles Location exceptions and only for Location controllers, and it runs before the
 * IAM handler. Everything else (validation errors, bad JSON, access denied, unexpected errors) is
 * left to the shared GlobalExceptionHandler, so the response format stays the same.
 */
@RestControllerAdvice(basePackages = "tsgp.yah_enterprise.location.presentation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocationExceptionHandler {

    @ExceptionHandler({LocationNotFoundException.class, LocationUserNotFoundException.class,
            UserNotAssignedException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({LocationCodeAlreadyExistsException.class, LocationAlreadyActiveException.class,
            LocationAlreadyInactiveException.class, LocationInactiveException.class,
            UserAlreadyAssignedException.class, UserNotAssignableException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidLocationException.class, InvalidLocationTypeException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidInput(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, request.getRequestURI()));
    }
}