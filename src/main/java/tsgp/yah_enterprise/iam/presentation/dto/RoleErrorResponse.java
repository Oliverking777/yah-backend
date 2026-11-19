package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * HTTP response body returned for every error.
 */
@Schema(description = "Error returned by the API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleErrorResponse(

        @Schema(description = "When the error happened (UTC)", example = "2026-09-29T10:15:30Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "HTTP status text", example = "Not Found")
        String error,

        @Schema(description = "What went wrong", example = "Role with id: 3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c was not found")
        String message,

        @Schema(description = "Request path", example = "/api/v1/roles/3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c")
        String path,

        @Schema(description = "Field-level problems, only present for validation errors (field -> message)",
                example = "{\"roleName\": \"Role name is required\"}")
        Map<String, String> fieldErrors
) {
    public static RoleErrorResponse of(HttpStatusCode status, String message, String path) {
        return new RoleErrorResponse(Instant.now(), status.value(), reasonPhrase(status), message, path, null);
    }

    public static RoleErrorResponse ofValidation(String message, String path, Map<String, String> fieldErrors) {
        return new RoleErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), message, path, Map.copyOf(fieldErrors));
    }

    private static String reasonPhrase(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.getReasonPhrase() : "Error";
    }
}