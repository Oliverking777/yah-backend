package tsgp.yah_enterprise.iam.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.ChangePasswordCommand;
import tsgp.yah_enterprise.iam.application.dto.CreateUserCommand;
import tsgp.yah_enterprise.iam.application.dto.UpdateUserCommand;
import tsgp.yah_enterprise.iam.application.dto.UserPageRequest;
import tsgp.yah_enterprise.iam.application.dto.UserPageResult;
import tsgp.yah_enterprise.iam.application.dto.UserResult;
import tsgp.yah_enterprise.iam.application.port.in.ChangeUserPasswordUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ChangeUserStatusUseCase;
import tsgp.yah_enterprise.iam.application.port.in.CreateUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.DeleteUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.GetUserUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ListUsersUseCase;
import tsgp.yah_enterprise.iam.application.port.in.UpdateUserUseCase;
import tsgp.yah_enterprise.iam.application.port.out.PasswordHasher;
import tsgp.yah_enterprise.iam.domain.exception.CredentialNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException;
import tsgp.yah_enterprise.iam.domain.model.Credential;
import tsgp.yah_enterprise.iam.domain.model.User;
import tsgp.yah_enterprise.iam.domain.repository.CredentialRepository;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;
import tsgp.yah_enterprise.iam.domain.service.UsernameGenerator;
import tsgp.yah_enterprise.iam.domain.valueobject.RawPassword;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Application service implementing all user use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements
        CreateUserUseCase,
        UpdateUserUseCase,
        GetUserUseCase,
        ListUsersUseCase,
        ChangeUserStatusUseCase,
        ChangeUserPasswordUseCase,
        DeleteUserUseCase {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public UserResult createUser(CreateUserCommand command) {
        // Check the password first, so a weak password is rejected before anything else happens.
        RawPassword rawPassword = RawPassword.of(command.password());

        requireRoleExists(command.roleId());

        Username userName = UsernameGenerator.generate(
                command.firstName(), command.lastName(), userRepository::existsByUserName);

        User user = User.createUser(command.firstName(), command.lastName(), userName, command.roleId());
        User saved = userRepository.save(user);

        Credential credential = Credential.createCredential(saved.getUserId(), passwordHasher.hash(rawPassword));
        credentialRepository.save(credential);

        return UserResult.from(saved);
    }

    @Override
    public UserResult updateUser(UUID userId, UpdateUserCommand command) {
        User user = findUser(userId);

        if (command.firstName() != null || command.lastName() != null) {
            String firstName = command.firstName() != null ? command.firstName() : user.getFirstName();
            String lastName = command.lastName() != null ? command.lastName() : user.getLastName();
            user.changeName(firstName, lastName);
        }

        if (command.roleId() != null) {
            requireRoleExists(command.roleId());
            user.changeRole(command.roleId());
        }

        return UserResult.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResult getUserById(UUID userId) {
        return UserResult.from(findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResult getUserByUserName(String userName) {
        Username username = Username.of(userName);
        return userRepository.findByUserName(username)
                .map(UserResult::from)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPageResult listUsers(UserPageRequest request) {
        List<UserResult> content = userRepository.findAll(request.page(), request.size())
                .stream()
                .map(UserResult::from)
                .toList();

        long total = userRepository.count();
        return UserPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public UserResult activateUser(UUID userId) {
        User user = findUser(userId);
        user.activate();
        return UserResult.from(userRepository.save(user));
    }

    @Override
    public UserResult deactivateUser(UUID userId) {
        User user = findUser(userId);
        user.deactivate();
        return UserResult.from(userRepository.save(user));
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordCommand command) {
        RawPassword rawPassword = RawPassword.of(command.newPassword());

        findUser(userId); // fails with UserNotFoundException if the user does not exist

        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new CredentialNotFoundException(userId));

        credential.changePassword(passwordHasher.hash(rawPassword));
        credentialRepository.save(credential);
    }

    @Override
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        credentialRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    // ---- helpers ----

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void requireRoleExists(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException(roleId);
        }
    }
}