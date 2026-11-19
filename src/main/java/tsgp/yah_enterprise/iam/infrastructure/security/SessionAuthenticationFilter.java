package tsgp.yah_enterprise.iam.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsgp.yah_enterprise.config.RoleCatalog;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;
import tsgp.yah_enterprise.iam.application.port.in.AuthenticateSessionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.RecordUserActivityUseCase;
import tsgp.yah_enterprise.iam.domain.exception.InvalidSessionTokenException;
import tsgp.yah_enterprise.iam.domain.exception.SessionExpiredException;
import tsgp.yah_enterprise.iam.domain.exception.SessionNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.UserInactiveException;
import tsgp.yah_enterprise.iam.infrastructure.security.SessionTokenResolver.ResolvedToken;
import tsgp.yah_enterprise.iam.infrastructure.security.SessionTokenResolver.TokenSource;

/**
 * Authenticates each request from its session token.
 * <p>
 * - Finds the token (Authorization header first, then the cookie).
 * - Validates it with the AuthenticateSessionUseCase and, if valid, puts the user in the security context,
 *   with the user's permission codes as authorities (so hasAuthority('ROLE_CREATE') works)
 *   and one more authority for the user's role (so hasAuthority('ROLE:OWNER') works).
 * - If the token is missing or invalid, the request simply continues unauthenticated; protected endpoints
 *   then answer 401 through RestAuthenticationEntryPoint.
 * <p>
 * CSRF rule for cookies: a browser attaches cookies automatically, so a request that CHANGES data
 * (anything but GET, HEAD, OPTIONS, TRACE) and is authenticated by cookie must also carry the configured
 * custom header (default X-Requested-With). Another site cannot add that header to a cross-site request.
 * Requests authenticated by the Authorization header are not affected, since browsers never add it by themselves.
 * <p>
 * This filter is deliberately NOT a Spring bean: Spring Boot would register every Filter bean globally as well,
 * outside the security chain. SecurityConfig creates it and adds it to the chain.
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute set when a token was present but rejected. Read by RestAuthenticationEntryPoint. */
    public static final String FAILURE_ATTRIBUTE = SessionAuthenticationFilter.class.getName() + ".FAILURE";
    public static final String FAILURE_EXPIRED = "EXPIRED";
    public static final String FAILURE_INVALID = "INVALID";

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationFilter.class);
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AuthenticateSessionUseCase authenticateSessionUseCase;
    private final RecordUserActivityUseCase recordUserActivityUseCase;
    private final SessionTokenResolver tokenResolver;
    private final SecurityProperties properties;
    private final JsonErrorWriter errorWriter;
    private final String loginPath;

    public SessionAuthenticationFilter(AuthenticateSessionUseCase authenticateSessionUseCase,
                                       RecordUserActivityUseCase recordUserActivityUseCase,
                                       SessionTokenResolver tokenResolver,
                                       SecurityProperties properties,
                                       JsonErrorWriter errorWriter,
                                       String loginPath) {
        this.authenticateSessionUseCase = authenticateSessionUseCase;
        this.recordUserActivityUseCase = recordUserActivityUseCase;
        this.tokenResolver = tokenResolver;
        this.properties = properties;
        this.errorWriter = errorWriter;
        this.loginPath = loginPath;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight carries no credentials. Login must work even if an old cookie is still around.
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || loginPath.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Optional<ResolvedToken> resolved = tokenResolver.resolve(request);
        if (resolved.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        ResolvedToken token = resolved.get();

        if (token.source() == TokenSource.COOKIE
                && !SAFE_METHODS.contains(request.getMethod().toUpperCase())
                && request.getHeader(properties.csrfHeader()) == null) {
            errorWriter.write(request, response, HttpStatus.FORBIDDEN,
                    "Missing required header: " + properties.csrfHeader());
            return;
        }

        try {
            AuthenticatedUser user = authenticateSessionUseCase.authenticate(token.value());

            // The user's permission codes, plus one authority for their role (e.g. "ROLE:OWNER").
            List<SimpleGrantedAuthority> authorities = Stream.concat(
                            user.permissions().stream(),
                            Stream.of(RoleCatalog.authorityOf(user.roleName())))
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, authorities));
            SecurityContextHolder.setContext(context);

            recordActivity(user);
        } catch (SessionExpiredException e) {
            request.setAttribute(FAILURE_ATTRIBUTE, FAILURE_EXPIRED);
            log.debug("Rejected a request with an expired session");
        } catch (InvalidSessionTokenException | SessionNotFoundException | UserInactiveException e) {
            request.setAttribute(FAILURE_ATTRIBUTE, FAILURE_INVALID);
            log.debug("Rejected a request with an unusable session: {}", e.getClass().getSimpleName());
        }

        chain.doFilter(request, response);
    }

    /**
     * Records that the user was active. The use case throttles itself, and a failure here
     * must never break the request, so it is only logged.
     */
    private void recordActivity(AuthenticatedUser user) {
        try {
            recordUserActivityUseCase.recordActivity(user.userId());
        } catch (RuntimeException e) {
            log.warn("Could not record activity for user {}: {}", user.userId(), e.toString());
        }
    }
}