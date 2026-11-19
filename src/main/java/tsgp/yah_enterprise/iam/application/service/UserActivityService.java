package tsgp.yah_enterprise.iam.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.iam.application.port.in.RecordUserActivityUseCase;
import tsgp.yah_enterprise.iam.domain.repository.UserRepository;

/**
 * Keeps the user's last activity time up to date.
 * Throttled: the value is rewritten at most once per THROTTLE, so a busy user does not cause
 * a database write on every request. The check is done inside the UPDATE itself.
 * This is a separate service from AuthService because authenticate() runs in a read-only transaction.
 */
@Service
@RequiredArgsConstructor
public class UserActivityService implements RecordUserActivityUseCase {

    static final Duration THROTTLE = Duration.ofMinutes(5);

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordActivity(UUID userId) {
        Instant now = Instant.now();
        userRepository.updateLastActiveAt(userId, now, now.minus(THROTTLE));
    }
}