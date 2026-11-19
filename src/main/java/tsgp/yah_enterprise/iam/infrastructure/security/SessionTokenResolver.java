package tsgp.yah_enterprise.iam.infrastructure.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Finds the session token in a request: the Authorization header first (mobile), then the cookie (web).
 * If an Authorization header is present it wins, even if its token turns out to be invalid,
 * so a bad header is never silently replaced by a cookie.
 */
@Component
@RequiredArgsConstructor
public class SessionTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecurityProperties properties;

    public enum TokenSource {
        HEADER,
        COOKIE
    }

    /**
     * A token found in a request, and where it came from. toString() never shows the token.
     */
    public record ResolvedToken(String value, TokenSource source) {
        @Override
        public String toString() {
            return "ResolvedToken[value=protected, source=" + source + "]";
        }
    }

    public Optional<ResolvedToken> resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            String value = header.substring(BEARER_PREFIX.length()).trim();
            if (!value.isEmpty()) {
                return Optional.of(new ResolvedToken(value, TokenSource.HEADER));
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (properties.cookie().name().equals(cookie.getName())
                        && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return Optional.of(new ResolvedToken(cookie.getValue(), TokenSource.COOKIE));
                }
            }
        }
        return Optional.empty();
    }
}