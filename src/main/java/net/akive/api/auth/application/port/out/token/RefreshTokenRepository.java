package net.akive.api.auth.application.port.out.token;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {

    Optional<String> findByUserId(Long userId);
    void save(Long userId, String refreshToken, Duration ttl);
    void delete(Long userId);
}
