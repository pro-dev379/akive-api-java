package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.domain.ClientInstanceId;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenStore {

    List<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByUserIdAndClientInstantId(Long userId, ClientInstanceId clientInstanceId);

    void save(Long userId, ClientInstanceId clientInstanceId, RefreshToken refreshToken, Duration ttl);

    boolean rotate(
            Long userId,
            ClientInstanceId clientInstanceId,
            RefreshToken currentRefreshToken,
            RefreshToken newRefreshToken,
            Duration ttl
    );

    void deleteByUserId(Long userId);

    void deleteByUserIdAndClientInstanceId(Long userId, ClientInstanceId clientInstanceId);
}
