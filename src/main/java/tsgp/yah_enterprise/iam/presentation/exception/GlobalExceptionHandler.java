package tsgp.yah_enterprise.iam.presentation.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.iam.domain.exception.CredentialNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.IncorrectCurrentPasswordException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidCredentialsException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidLocationScopeException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPermissionCodeException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidRoleDescriptionException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidUsernameException;
import tsgp.yah_enterprise.iam.domain.exception.PermissionAlreadyExistsException;
import tsgp.yah_enterprise.iam.domain.exception.PermissionInUseException;
import tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.RoleAlreadyExistsException;
import tsgp.yah_enterprise.iam.domain.exception.RoleInUseException;
import tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.SystemRoleProtectedException;
import tsgp.yah_enterprise.iam.domain.exception.UserAlreadyExistsException;
import tsgp.yah_enterprise.iam.domain.exception.UserInactiveException;
import tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException;

/**
 * Turns exceptions into ApiResponse bodies.
 * Extends ResponseEntityExceptionHandler so standard Spring MVC errors
 * (bad JSON, malformed UUID, wrong HTTP method...) use the same format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.beans.factory.annotation.Autowired
    private tsgp.yah_enterprise.iam.infrastructure.security.SecurityAuditRecorder auditRecorder;


    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleAlreadyExists(RoleAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                 HttpServletRequest request) {
        // Typically the unique constraint on role_name when two requests race.
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
    }

    @ExceptionHandler({InvalidRoleNameException.class, InvalidRoleDescriptionException.class,
            InvalidLocationScopeException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRoleInput(RuntimeException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionNotFound(PermissionNotFoundException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PermissionAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionAlreadyExists(PermissionAlreadyExistsException ex,
                                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(PermissionInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermissionInUse(PermissionInUseException ex,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPermissionCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPermissionCode(InvalidPermissionCodeException ex,
                                                                         HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleInUse(RoleInUseException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(SystemRoleProtectedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemRoleProtected(SystemRoleProtectedException ex,
                                                                       HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({UserNotFoundException.class, CredentialNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidUsernameException.class, InvalidPersonNameException.class,
            InvalidPasswordException.class, IncorrectCurrentPasswordException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserInput(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserInactive(UserInactiveException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Thrown by @PreAuthorize checks inside controllers. Without this handler the catch-all below would turn it into a 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        auditRecorder.accessDenied(request.getMethod(), request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication required.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // ---- Standard Spring MVC errors, converted to the same format ----

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ApiResponse<Void> body = ApiResponse.validationError("Validation failed", pathOf(request), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        String message = (body instanceof ProblemDetail problem && problem.getDetail() != null)
                ? problem.getDetail()
                : ex.getMessage();

        ApiResponse<Void> error = ApiResponse.error(statusCode, message, pathOf(request));
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    // ---- helpers ----

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message, request.getRequestURI()));
    }

    private static String pathOf(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : null;
    }
}