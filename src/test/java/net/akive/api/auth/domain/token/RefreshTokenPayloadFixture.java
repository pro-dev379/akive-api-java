package net.akive.api.auth.domain.token;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.UserRole;

import java.time.Instant;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.EXPIRE_AT;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.UserRole.MEMBER;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class RefreshTokenPayloadFixture {

    private RefreshTokenPayloadFixture() {
    }

    public static RefreshTokenPayload createRefreshTokenPayload() {
        return createRefreshTokenPayload(USER_ID, createClientInstanceId(), MEMBER, EXPIRE_AT);
    }

    public static RefreshTokenPayload createRefreshTokenPayload(
            Long userId,
            ClientInstanceId clientInstanceId,
            UserRole role
    ) {
        return createRefreshTokenPayload(userId, clientInstanceId, role, EXPIRE_AT);
    }

    public static RefreshTokenPayload createRefreshTokenPayload(
            Long userId,
            ClientInstanceId clientInstanceId,
            UserRole role,
            Instant expiresAt
    ) {
        return new RefreshTokenPayload(userId, clientInstanceId, role, expiresAt);
    }
}
