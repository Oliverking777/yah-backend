package tsgp.yah_enterprise.iam.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Global OpenAPI (Swagger) metadata.
 * Swagger UI:   http://localhost:8080/swagger-ui.html
 * API document: http://localhost:8080/v3/api-docs
 * <p>
 * To try protected endpoints in Swagger UI: call POST /api/v1/auth/login with clientType MOBILE,
 * copy "sessionToken" from the response, click "Authorize" and paste it.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI yahEnterpriseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Yah Enterprise API")
                        .description("REST API for the Yah Enterprise platform (IAM module).")
                        .version("v1")
                        .contact(new Contact().name("TSGP Corporation")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("The session token returned by POST /api/v1/auth/login (clientType MOBILE).")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}