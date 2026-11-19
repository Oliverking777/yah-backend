package tsgp.yah_enterprise.iam.infrastructure.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings under "app.security" in application.properties.
 *
 * @param cookie         how the session cookie (used by web clients) is set
 * @param allowedOrigins browser origins allowed to call the API (CORS), e.g. the dashboard
 * @param csrfHeader     header that must be present on state-changing requests authenticated by cookie
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @DefaultValue CookieProperties cookie,
        @DefaultValue("http://localhost:3000") List<String> allowedOrigins,
        @DefaultValue("X-Requested-With") String csrfHeader
) {

    /**
     * @param name     cookie name
     * @param secure   send the cookie over HTTPS only. Must be true in production; false lets it work on http://localhost
     * @param sameSite Lax, Strict or None
     */
    public record CookieProperties(
            @DefaultValue("YAH_SESSION") String name,
            @DefaultValue("false") boolean secure,
            @DefaultValue("Lax") String sameSite
    ) {
    }
}