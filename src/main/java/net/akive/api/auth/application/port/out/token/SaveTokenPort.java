package net.akive.api.auth.application.port.out.token;

import java.time.Duration;

public interface SaveTokenPort {

    void save(Long userId, String refreshToken, Duration ttl);
}
