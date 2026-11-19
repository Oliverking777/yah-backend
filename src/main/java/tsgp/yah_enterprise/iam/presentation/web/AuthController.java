package tsgp.yah_enterprise.iam.presentation.web;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.config.ApiResponse;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;
import tsgp.yah_enterprise.iam.application.dto.LoginCommand;
import tsgp.yah_enterprise.iam.application.dto.LoginResult;
import tsgp.yah_enterprise.iam.application.port.in.ChangeOwnPasswordUseCase;
import tsgp.yah_enterprise.iam.application.port.in.LoginUseCase;
import tsgp.yah_enterprise.iam.application.port.in.LogoutUseCase;
import tsgp.yah_enterprise.iam.domain.model.ClientType;
import tsgp.yah_enterprise.iam.infrastructure.security.SessionCookieFactory;
import tsgp.yah_enterprise.iam.infrastructure.security.SessionTokenResolver;
import tsgp.yah_enterprise.iam.presentation.dto.ChangeOwnPasswordRequest;
import tsgp.yah_enterprise.iam.presentation.dto.CurrentUserResponse;
import tsgp.yah_enterprise.iam.presentation.dto.LoginRequest;
import tsgp.yah_enterprise.iam.presentation.dto.LoginResponse;

/**
 * Note: our shared ApiResponse class has the same simple name as Swagger's @ApiResponse annotation,
 * so the annotation is written with its full package name below.
 */
@RestController
@RequestMapping("/yah/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Log in, log out, and manage your own session and password")
public class AuthController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChangeOwnPasswordUseCase changeOwnPasswordUseCase;
    private final SessionTokenResolver tokenResolver;
    private final SessionCookieFactory cookieFactory;
    private final tsgp.yah_enterprise.iam.infrastructure.security.SecurityAuditRecorder auditRecorder;

    @PostMapping("/login")
    @SecurityRequirements // public endpoint: no token needed
    @Operation(summary = "Log in",
            description = "Starts a session that lasts exactly 12 hours and is never extended. "
                    + "A new login ends the user's previous session. "
                    + "WEB clients get an HttpOnly cookie; MOBILE clients get the token in the response body.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged in"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Wrong username or password",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "The account is deactivated",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest) {
        LoginResult result;
        try {
            result = loginUseCase.login(new LoginCommand(
                    request.userName(),
                    request.password(),
                    request.clientType(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader(HttpHeaders.USER_AGENT)));
        } catch (tsgp.yah_enterprise.iam.domain.exception.InvalidCredentialsException
                 | tsgp.yah_enterprise.iam.domain.exception.UserInactiveException e) {
            auditRecorder.loginFailed(request.userName(), e instanceof
                    tsgp.yah_enterprise.iam.domain.exception.UserInactiveException
                    ? "The account is deactivated" : "Wrong username or password");
            throw e;
        }

        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .cacheControl(CacheControl.noStore()); // the response may contain a token

        if (result.clientType() == ClientType.WEB) {
            Duration maxAge = Duration.between(Instant.now(), result.expiresAt());
            response.header(HttpHeaders.SET_COOKIE, cookieFactory.create(result.sessionToken(), maxAge).toString());
        }
        return response.body(ApiResponse.ok("Logged in", LoginResponse.from(result)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out",
            description = "Ends the current session and clears the cookie. Always answers 204, "
                    + "even if the session was already over, so it is safe to call twice.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logged out")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        tokenResolver.resolve(httpRequest).ifPresent(token -> logoutUseCase.logout(token.value()));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Who am I",
            description = "Returns the logged-in user, their role, their permission codes and when the session ends.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "The current user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in, or the session has ended",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<CurrentUserResponse> me(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(CurrentUserResponse.from(user));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change my own password",
            description = "Needs the current password. The session stays open.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Wrong current password, or the new password breaks the rules",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in, or the session has ended",
                    content = @Content(mediaType = JSON, schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Void> changeOwnPassword(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
                                                  @Valid @RequestBody ChangeOwnPasswordRequest request) {
        changeOwnPasswordUseCase.changeOwnPassword(user.userId(), request.toCommand());
        return ResponseEntity.noContent().build();
    }
}