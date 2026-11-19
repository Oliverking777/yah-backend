package tsgp.yah_enterprise.iam.infrastructure.security;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Builds the session cookie for web clients: HttpOnly (JavaScript cannot read it), SameSite, and Secure when configured.
 * Its Max-Age is the time left until the session's fixed expiry.
 * Used by the login and logout endpoints.
 */
@Component
@RequiredArgsConstructor
public class SessionCookieFactory {

    private final SecurityProperties properties;

    public ResponseCookie create(String sessionToken, Duration maxAge) {
        return base(sessionToken).maxAge(maxAge).build();
    }

    /**
     * A cookie that makes the browser delete the session cookie.
     */
    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        SecurityProperties.CookieProperties cookie = properties.cookie();
        return ResponseCookie.from(cookie.name(), value)
                .httpOnly(true)
                .secure(cookie.secure())
                .sameSite(cookie.sameSite())
                .path("/");
    }
}