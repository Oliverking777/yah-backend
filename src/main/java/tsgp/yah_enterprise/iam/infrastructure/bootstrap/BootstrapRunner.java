package tsgp.yah_enterprise.iam.infrastructure.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersCommand;
import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersResult;
import tsgp.yah_enterprise.iam.application.port.in.InitializeFirstUsersUseCase;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException;

/**
 * Runs once at every startup, after the application is up. Creates the first ADMIN and OWNER users
 * on an empty system. It never logs a password.
 */
@Component
@RequiredArgsConstructor
public class BootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapRunner.class);

    private final InitializeFirstUsersUseCase initializeFirstUsersUseCase;
    private final BootstrapProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        BootstrapProperties.AccountProperties admin = properties.admin();
        BootstrapProperties.AccountProperties owner = properties.owner();

        InitializeFirstUsersResult result;
        try {
            result = initializeFirstUsersUseCase.initialize(new InitializeFirstUsersCommand(
                    admin.firstName(), admin.lastName(), admin.password(),
                    owner.firstName(), owner.lastName(), owner.password()));
        } catch (InvalidPasswordException | InvalidPersonNameException e) {
            // Fail loudly: starting without the users the operator asked for would be confusing.
            throw new IllegalStateException("The bootstrap user settings are invalid: " + e.getMessage()
                    + " Check the BOOTSTRAP_ADMIN_* and BOOTSTRAP_OWNER_* environment variables.", e);
        }

        if (result.usersAlreadyExist()) {
            return;
        }
        if (result.admin() != null) {
            log.info("Bootstrap: created the first ADMIN user '{}'.", result.admin().userName());
        }
        if (result.owner() != null) {
            log.info("Bootstrap: created the first OWNER user '{}'.", result.owner().userName());
        }
        if (result.admin() != null || result.owner() != null) {
            log.info("Bootstrap: log in, change the password(s), then remove the BOOTSTRAP_*_PASSWORD "
                    + "variables from the environment.");
        }
        if (result.nothingConfigured()) {
            log.warn("Bootstrap: there are no users and no bootstrap password is set, so NO user was created "
                    + "and nobody can log in. Set BOOTSTRAP_ADMIN_PASSWORD and restart.");
        }
    }
}
