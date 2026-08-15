package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.domain.ClientInstanceId;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

    Optional<RefreshToken> findByUserIdAndClientInstantId(Long userId, ClientInstanceId clientInstanceId);

    void save(Long userId, ClientInstanceId clientInstanceId, RefreshToken refreshToken, Duration ttl);

    boolean rotate(
            Long userId,
            ClientInstanceId clientInstanceId,
            RefreshToken currentRefreshToken,
            RefreshToken newRefreshToken,
            Duration ttl
    );

    void delete(Long userId, ClientInstanceId clientInstanceId);
}
