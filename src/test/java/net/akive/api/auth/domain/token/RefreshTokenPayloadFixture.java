package net.akive.api.auth.domain.token;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

import java.time.Instant;

import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.RoleType.USER;

public class RefreshTokenPayloadFixture {

    public static final Instant REFRESH_TOKEN_EXPIRES_AT = Instant.parse("2026-07-03T00:01:00Z");

    public static RefreshTokenPayload createRefreshTokenPayload() {
        return createRefreshTokenPayload(1L, createClientInstanceId(), USER, REFRESH_TOKEN_EXPIRES_AT);
    }

    public static RefreshTokenPayload createRefreshTokenPayload(
            Long userId,
            ClientInstanceId clientInstanceId,
            RoleType roleType
    ) {
        return createRefreshTokenPayload(userId, clientInstanceId, roleType, REFRESH_TOKEN_EXPIRES_AT);
    }

    public static RefreshTokenPayload createRefreshTokenPayload(
            Long userId,
            ClientInstanceId clientInstanceId,
            RoleType roleType,
            Instant expiresAt
    ) {
        return new RefreshTokenPayload(userId, clientInstanceId, roleType, expiresAt);
    }
}
