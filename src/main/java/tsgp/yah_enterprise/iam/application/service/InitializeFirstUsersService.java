package tsgp.yah_enterprise.iam.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.CreateUserCommand;
import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersCommand;
import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersResult;
import tsgp.yah_enterprise.iam.application.dto.UserResult;
import tsgp.yah_enterprise.iam.application.port.in.CreateUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.InitializeFirstUsersUseCase;
import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

/**
 * Creates the first ADMIN and OWNER users. See {@link InitializeFirstUsersUseCase}.
 * It never creates roles or permissions: those are seeded by the Flyway migrations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InitializeFirstUsersService implements InitializeFirstUsersUseCase {

    static final String ADMIN_ROLE_NAME = "ADMIN";
    static final String OWNER_ROLE_NAME = "OWNER";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;

    @Override
    public InitializeFirstUsersResult initialize(InitializeFirstUsersCommand command) {
        if (userRepository.count() > 0) {
            return new InitializeFirstUsersResult(true, null, null);
        }
        UserResult admin = createIfConfigured(ADMIN_ROLE_NAME,
                command.adminFirstName(), command.adminLastName(), command.adminPassword());
        UserResult owner = createIfConfigured(OWNER_ROLE_NAME,
                command.ownerFirstName(), command.ownerLastName(), command.ownerPassword());
        return new InitializeFirstUsersResult(false, admin, owner);
    }

    private UserResult createIfConfigured(String roleName, String firstName, String lastName, String password) {
        if (password == null || password.isBlank()) {
            return null;
        }
        Role role = roleRepository.findByName(RoleName.of(roleName))
                .orElseThrow(() -> new IllegalStateException(
                        "The " + roleName + " role does not exist. Did the Flyway migration V10 run?"));
        return createUserUseCase.createUser(new CreateUserCommand(firstName, lastName, role.getRoleId(), password));
    }
}