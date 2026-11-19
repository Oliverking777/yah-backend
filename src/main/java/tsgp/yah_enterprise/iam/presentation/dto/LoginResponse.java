package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.LoginResult;
import tsgp.yah_enterprise.iam.domain.model.ClientType;

/**
 * HTTP response body of a successful login.
 * For WEB clients the token is left out: it travels only in the HttpOnly cookie, out of reach of JavaScript.
 */
@Schema(description = "Result of a successful login")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(

        @Schema(description = "Session token. Only present for MOBILE clients. "
                + "Send it as 'Authorization: Bearer <token>'. Keep it in the platform's secure storage.")
        String sessionToken,

        @Schema(description = "When the session ends (UTC). Fixed at login: it is never extended.",
                example = "2026-09-30T22:15:30Z")
        Instant expiresAt,

        @Schema(description = "The client type the session was opened for")
        ClientType clientType,

        @Schema(description = "The logged-in user")
        UserResponse user
) {
    /**
     * Builds the response for a login result. The token is included only for MOBILE clients.
     */
    public static LoginResponse from(LoginResult result) {
        String token = result.clientType() == ClientType.MOBILE ? result.sessionToken() : null;
        return new LoginResponse(token, result.expiresAt(), result.clientType(), UserResponse.from(result.user()));
    }

    @Override
    public String toString() {
        return "LoginResponse[sessionToken=" + (sessionToken == null ? "absent" : "protected")
                + ", expiresAt=" + expiresAt + ", clientType=" + clientType + ", user=" + user + "]";
    }
}