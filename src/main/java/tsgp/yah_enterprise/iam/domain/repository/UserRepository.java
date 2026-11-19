package tsgp.yah_enterprise.iam.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.User;
import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Repository interface (port) for managing users.
 * The implementation lives in the infrastructure layer.
 */
public interface UserRepository {

    /**
     * Saves a new user or updates an existing one.
     */
    User save(User user);

    Optional<User> findById(UUID userId);

    Optional<User> findByUserName(Username userName);

    /**
     * Returns one page of users, ordered by username.
     *
     * @param page zero-based page index
     * @param size number of users per page
     */
    List<User> findAll(int page, int size);

    /**
     * Returns the total number of users.
     */
    long count();

    boolean existsById(UUID userId);

    boolean existsByUserName(Username userName);

    /**
     * Returns true if at least one user has the given role.
     */
    boolean existsByRoleId(UUID roleId);

    /**
     * Sets the user's last activity time to activeAt, but only if it is empty or older than staleBefore.
     * Done in one statement, without loading the user, so it cannot overwrite other fields
     * and is safe when two requests arrive together.
     *
     * @return true if the value was written, false if it was recent enough to leave alone
     */
    boolean updateLastActiveAt(UUID userId, Instant activeAt, Instant staleBefore);

    void deleteById(UUID userId);
}