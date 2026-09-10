package net.akive.api.auth.application.token.provided;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.user.domain.RoleType;

import java.util.Optional;

public interface RefreshTokenSessionManager {

    RefreshToken start(Long userId, ClientInstanceId clientInstanceId, RoleType roleType);

    Optional<RefreshToken> rotateIfRequired(RefreshTokenPayload payload, RefreshToken currentRefreshToken)
            throws AuthenticationException;

    void revokeByUserId(Long userId);

    void revokeByUserIdAndClientInstanceId(Long userId, ClientInstanceId clientInstanceId);
}
