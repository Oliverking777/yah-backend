package tsgp.yah_enterprise.iam.infrastructure.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings under "app.bootstrap" in application.properties. They are only used when the system has no users.
 * Set the passwords with environment variables (see application.properties), never in source code.
 */
@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(
        @DefaultValue AccountProperties admin,
        @DefaultValue AccountProperties owner
) {

    /**
     * @param password  empty means: do not create this user
     * @param firstName first name of the user
     * @param lastName  last name of the user
     */
    public record AccountProperties(
            @DefaultValue("") String password,
            @DefaultValue("") String firstName,
            @DefaultValue("") String lastName
    ) {
        @Override
        public String toString() {
            return "AccountProperties[password=protected, firstName=" + firstName + ", lastName=" + lastName + "]";
        }
    }
}