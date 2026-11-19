package tsgp.yah_enterprise.iam.application.service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;
import tsgp.yah_enterprise.iam.application.dto.ChangeOwnPasswordCommand;
import tsgp.yah_enterprise.iam.application.dto.LoginCommand;
import tsgp.yah_enterprise.iam.application.dto.LoginResult;
import tsgp.yah_enterprise.iam.application.dto.UserResult;
import tsgp.yah_enterprise.iam.application.port.in.AuthenticateSessionUseCase;
import tsgp.yah_enterprise.iam.application.port.in.ChangeOwnPasswordUseCase;
import tsgp.yah_enterprise.iam.application.port.in.LoginUseCase;
import tsgp.yah_enterprise.iam.application.port.in.LogoutUseCase;
import tsgp.yah_enterprise.iam.application.port.out.PasswordHasher;
import tsgp.yah_enterprise.iam.domain.exception.CredentialNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.IncorrectCurrentPasswordException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidCredentialsException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidSessionTokenException;
import tsgp.yah_enterprise.iam.domain.exception.InvalidUsernameException;
import tsgp.yah_enterprise.iam.domain.exception.SessionExpiredException;
import tsgp.yah_enterprise.iam.domain.exception.SessionNotFoundException;
import tsgp.yah_enterprise.iam.domain.exception.UserInactiveException;
import tsgp.yah_enterprise.iam.domain.model.Credential;
import tsgp.yah_enterprise.iam.domain.model.Permission;
import tsgp.yah_enterprise.iam.domain.model.Role;
import tsgp.yah_enterprise.iam.domain.model.Session;
import tsgp.yah_enterprise.iam.domain.model.User;
import tsgp.yah_enterprise.iam.domain.repository.CredentialRepository;
import tsgp.yah_enterprise.iam.domain.repository.PermissionRepository;
import tsgp.yah_enterprise.iam.domain.repository.RoleRepository;
import tsgp.yah_enterprise.iam.domain.repository.SessionRepository;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;
import tsgp.yah_enterprise.iam.domain.valueobject.RawPassword;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionToken;
import tsgp.yah_enterprise.iam.domain.valueobject.SessionTokenHash;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Application service for login, logout, session validation and changing one's own password.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase, LogoutUseCase, AuthenticateSessionUseCase, ChangeOwnPasswordUseCase {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final SessionRepository sessionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        String password = command.password() == null ? "" : command.password();

        User user = findUserForLogin(command.userName(), password);
        Credential credential = credentialRepository.findByUserId(user.getUserId()).orElse(null);

        if (credential == null) {
            passwordHasher.simulateCheck(password);
            throw new InvalidCredentialsException();
        }
        if (!passwordHasher.matches(password, credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Only checked after the password is right, so nobody without the password can learn the account is inactive.
        if (!user.isActive()) {
            throw new UserInactiveException(user.getUserName());
        }

        SessionToken token = SessionToken.generate();
        Session session = Session.start(
                user.getUserId(),
                SessionTokenHash.from(token),
                command.clientType(),
                command.ipAddress(),
                command.userAgent());

        // One session per user: end the old one first. This delete runs immediately (bulk query).
        sessionRepository.deleteByUserId(user.getUserId());
        Session saved = sessionRepository.save(session);

        // Logging in counts as activity (the session filter skips the login path).
        // staleBefore = now, so the value is always written.
        Instant now = Instant.now();
        userRepository.updateLastActiveAt(user.getUserId(), now, now);

        return new LoginResult(token.value(), saved.getExpiresAt(), saved.getClientType(), UserResult.from(user));
    }

    @Override
    @Transactional
    public void logout(String sessionToken) {
        SessionToken token;
        try {
            token = SessionToken.of(sessionToken);
        } catch (InvalidSessionTokenException e) {
            return; // nothing to end
        }
        sessionRepository.findByTokenHash(SessionTokenHash.from(token))
                .ifPresent(session -> sessionRepository.deleteByUserId(session.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(String sessionToken) {
        SessionToken token = SessionToken.of(sessionToken);

        Session session = sessionRepository.findByTokenHash(SessionTokenHash.from(token))
                .orElseThrow(SessionNotFoundException::new);

        // Expired sessions are not deleted here: a later login replaces them and a cleanup job removes the rest.
        if (session.isExpired()) {
            throw new SessionExpiredException(session.getSessionId());
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(SessionNotFoundException::new);
        if (!user.isActive()) {
            throw new UserInactiveException(user.getUserName());
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(SessionNotFoundException::new);

        Set<String> permissionCodes = permissionRepository.findAllByIds(role.getPermissionIds()).stream()
                .map(Permission::getPermissionCode)
                .map(code -> code.value())
                .collect(Collectors.toSet());

        return new AuthenticatedUser(
                user.getUserId(),
                user.getUserName().value(),
                user.getFullName(),
                role.getRoleId(),
                role.getRoleName().value(),
                role.getLocationScope(),
                permissionCodes,
                session.getSessionId(),
                session.getExpiresAt());
    }

    @Override
    @Transactional
    public void changeOwnPassword(UUID userId, ChangeOwnPasswordCommand command) {
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new CredentialNotFoundException(userId));

        String currentPassword = command.currentPassword() == null ? "" : command.currentPassword();
        if (!passwordHasher.matches(currentPassword, credential.getPasswordHash())) {
            throw new IncorrectCurrentPasswordException();
        }

        RawPassword newPassword = RawPassword.of(command.newPassword());
        if (passwordHasher.matches(newPassword.value(), credential.getPasswordHash())) {
            throw new InvalidPasswordException("The new password must be different from the current one.");
        }

        credential.changePassword(passwordHasher.hash(newPassword));
        credentialRepository.save(credential);
    }

    // ---- helpers ----

    /**
     * Finds the user for a login attempt. An unknown or malformed username behaves exactly like a wrong password:
     * same error, and about the same time spent.
     */
    private User findUserForLogin(String rawUserName, String password) {
        Username username;
        try {
            username = Username.of(rawUserName);
        } catch (InvalidUsernameException e) {
            passwordHasher.simulateCheck(password);
            throw new InvalidCredentialsException();
        }

        return userRepository.findByUserName(username).orElseGet(() -> {
            passwordHasher.simulateCheck(password);
            throw new InvalidCredentialsException();
        });
    }
}