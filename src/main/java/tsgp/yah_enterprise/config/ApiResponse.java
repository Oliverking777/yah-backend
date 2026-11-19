package tsgp.yah_enterprise.config;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard response envelope shared by every module.
 * Success:  { success: true,  status, message, data, timestamp }
 * Failure:  { success: false, status, message, fieldErrors?, path, timestamp }
 * Fields that are null are left out of the JSON.
 *
 * @param <T> type of the payload carried in "data"
 */
@Schema(description = "Standard response envelope used by every endpoint")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "true when the request succeeded", example = "true")
        boolean success,

        @Schema(description = "HTTP status code", example = "200")
        int status,

        @Schema(description = "Human-readable summary of the result", example = "Success")
        String message,

        @Schema(description = "The payload. Present on success only.")
        T data,

        @Schema(description = "Field-level problems (field -> message). Present on validation errors only.",
                example = "{\"roleName\": \"Role name is required\"}")
        Map<String, String> fieldErrors,

        @Schema(description = "Request path. Present on errors only.", example = "/api/v1/roles")
        String path,

        @Schema(description = "When the response was created (UTC)", example = "2026-09-29T10:15:30Z")
        Instant timestamp
) {

    // ---------- success ----------

    public static <T> ApiResponse<T> ok(T data) {
        return ok("Success", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, HttpStatus.OK.value(), message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, HttpStatus.CREATED.value(), message, data, null, null, Instant.now());
    }

    // ---------- errors ----------

    public static ApiResponse<Void> error(HttpStatusCode status, String message, String path) {
        return new ApiResponse<>(false, status.value(), message, null, null, path, Instant.now());
    }

    public static ApiResponse<Void> validationError(String message, String path, Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), message, null,
                Map.copyOf(fieldErrors), path, Instant.now());
    }
}