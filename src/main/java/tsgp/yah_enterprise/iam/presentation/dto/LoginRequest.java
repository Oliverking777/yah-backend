package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.domain.model.ClientType;

/**
 * HTTP request body for logging in.
 */
@Schema(description = "Login credentials")
public record LoginRequest(

        @Schema(description = "Username (not case-sensitive)", example = "john.doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username is too long")
        String userName,

        @Schema(description = "Password", example = "MyPassw0rd", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(max = 200, message = "Password is too long")
        String password,

        @Schema(description = "WEB: the session token is set in an HttpOnly cookie and is NOT returned in the body. "
                + "MOBILE: the token is returned in the body, to send as 'Authorization: Bearer <token>'. "
                + "Use MOBILE when trying the API in Swagger UI.",
                example = "MOBILE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Client type is required (WEB or MOBILE)")
        ClientType clientType
) {
    @Override
    public String toString() {
        return "LoginRequest[userName=" + userName + ", password=protected, clientType=" + clientType + "]";
    }
}