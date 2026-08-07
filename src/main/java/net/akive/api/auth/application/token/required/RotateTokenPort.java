package net.akive.api.auth.application.token.required;

import java.time.Duration;

public interface RotateTokenPort {

    boolean rotate(Long userId, String currentRefreshToken, String newRefreshToken, Duration ttl);
}
