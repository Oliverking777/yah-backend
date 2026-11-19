package tsgp.yah_enterprise.iam.infrastructure.security;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.port.in.AuthenticateSessionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.RecordUserActivityUseCase;

/**
 * Spring Security setup for server-side sessions with opaque tokens (no JWT).
 * <p>
 * - Stateless from Spring's point of view: we manage the sessions ourselves (sessions table).
 * - No form login, no HTTP Basic, no Spring logout: the auth endpoints handle login and logout.
 * - Everything requires a valid session, except the public paths below.
 * - Endpoints can require a permission with @PreAuthorize("hasAuthority('ROLE_CREATE')").
 *   Always use hasAuthority: hasRole would add a second "ROLE_" prefix to our codes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String LOGIN_PATH = "/yah/api/v1/auth/login";
    public static final String LOGOUT_PATH = "/yah/api/v1/auth/logout";

    private final SecurityProperties properties;
    private final SessionTokenResolver tokenResolver;
    private final AuthenticateSessionUseCase authenticateSessionUseCase;
    private final RecordUserActivityUseCase recordUserActivityUseCase;
    private final JsonErrorWriter errorWriter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SessionAuthenticationFilter sessionFilter = new SessionAuthenticationFilter(
                authenticateSessionUseCase, recordUserActivityUseCase, tokenResolver, properties, errorWriter, LOGIN_PATH);

        http
                // Not the built-in CSRF: sessions are not kept by Spring. The cookie rule is enforced in the filter.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(LOGIN_PATH).permitAll()
                        // Logout is public so it can clear the cookie even when the session has already ended.
                        // It is harmless: it only ends the session that the request's own token belongs to.
                        .requestMatchers(LOGOUT_PATH).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(sessionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Lets the browser dashboard call the API. Credentials (the cookie) are allowed, so origins must be listed explicitly.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", properties.csrfHeader()));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Stops Spring Boot from creating its default in-memory user with a generated password (printed in the log).
     * Nothing here logs in through Spring's own user system.
     */
    @Bean
    public UserDetailsService noUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Spring's user system is not used");
        };
    }
}