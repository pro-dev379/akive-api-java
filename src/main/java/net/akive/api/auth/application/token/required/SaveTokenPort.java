package net.akive.api.auth.application.token.required;

import java.time.Duration;

public interface SaveTokenPort {

    void save(Long userId, String refreshToken, Duration ttl);
}
