package tsgp.yah_enterprise.iam.infrastructure.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the bootstrap settings so they can be injected.
 */
@Configuration
@EnableConfigurationProperties(BootstrapProperties.class)
public class BootstrapConfig {
}