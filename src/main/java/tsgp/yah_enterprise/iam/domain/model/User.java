package tsgp.yah_enterprise.iam.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Domain model representing a user of the system.
 * The user's role is referenced by id so User and Role stay independent.
 * The password is NOT part of User: it lives in Credential.
 * Identity is defined by userId only.
 */
@Getter
public class User {

    private static final int MAX_NAME_LENGTH = 100;

    private final UUID userId;
    private String firstName;
    private String lastName;
    /** Always firstName + " " + lastName. Kept in sync by the class itself; it cannot be set from outside. */
    private String fullName;
    private final Username userName;
    private UUID roleId;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
    /**
     * When the user last made an authenticated request. Null if never.
     * Read-only here: it is written through UserRepository.updateLastActiveAt, so a normal save() never touches it.
     */
    private final Instant lastActiveAt;

    private User(UUID userId, String firstName, String lastName, Username userName, UUID roleId,
                 boolean active, Instant createdAt, Instant updatedAt, Instant lastActiveAt) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.firstName = requireValidName("firstName", firstName);
        this.lastName = requireValidName("lastName", lastName);
        this.fullName = buildFullName(this.firstName, this.lastName);
        this.userName = Objects.requireNonNull(userName, "userName must not be null");
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.lastActiveAt = lastActiveAt; // null until the first activity
    }

    /**
     * Creates a brand new, active user with a freshly generated id.
     * The username is not typed by anyone: generate it first with
     * {@link tsgp.yah_enterprise.iam.domain.service.UsernameGenerator}.
     * Once created, the username never changes.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException if a name is invalid
     */
    public static User createUser(String firstName, String lastName, Username userName, UUID roleId) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), firstName, lastName, userName, roleId, true, now, now, null);
    }

    /**
     * Rebuilds an existing user from stored data (e.g. from the database).
     */
    public static User reconstructUser(UUID userId, String firstName, String lastName, String userName,
                                       UUID roleId, boolean active, Instant createdAt, Instant updatedAt,
                                       Instant lastActiveAt) {
        return new User(userId, firstName, lastName, Username.of(userName), roleId, active, createdAt, updatedAt,
                lastActiveAt);
    }

    public void changeName(String newFirstName, String newLastName) {
        String first = requireValidName("firstName", newFirstName);
        String last = requireValidName("lastName", newLastName);
        this.firstName = first;
        this.lastName = last;
        this.fullName = buildFullName(first, last);
        touch();
    }

    public void changeRole(UUID newRoleId) {
        Objects.requireNonNull(newRoleId, "roleId must not be null");
        if (!this.roleId.equals(newRoleId)) {
            this.roleId = newRoleId;
            touch();
        }
    }

    public void activate() {
        if (!active) {
            this.active = true;
            touch();
        }
    }

    public void deactivate() {
        if (active) {
            this.active = false;
            touch();
        }
    }

    // ---------- helpers ----------

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String buildFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    private static String requireValidName(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPersonNameException(field, "It must not be blank.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new InvalidPersonNameException(field, "It must not exceed " + MAX_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", userName=" + userName + ", active=" + active + "}";
    }
}