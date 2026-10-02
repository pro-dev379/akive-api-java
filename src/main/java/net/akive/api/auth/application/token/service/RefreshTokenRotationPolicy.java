package net.akive.api.auth.application.token.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
class RefreshTokenRotationPolicy {

    private static final Duration ROTATION_THRESHOLD = Duration.ofMinutes(1);

    private final CurrentTimeProvider currentTimeProvider;

    boolean requiresRotation(Instant expiresAt) {
        Instant now = currentTimeProvider.now();
        Duration remaining = Duration.between(now, expiresAt);

        return remaining.compareTo(Duration.ZERO) > 0 && remaining.compareTo(ROTATION_THRESHOLD) <= 0;
    }
}
