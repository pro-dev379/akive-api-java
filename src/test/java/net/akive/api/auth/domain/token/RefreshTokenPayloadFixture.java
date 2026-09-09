package net.akive.api.auth.domain.token;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.RoleType;

import java.time.Instant;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.EXPIRE_AT;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.RoleType.MEMBER;
import static net.akive.api.user.domain.UserFixture.USER_ID;

public class RefreshTokenPayloadFixture {

    public static RefreshTokenPayload createRefreshTokenPayload() {
        return createRefreshTokenPayload(USER_ID, createClientInstanceId(), MEMBER, EXPIRE_AT);
    }

    public static RefreshTokenPayload createRefreshTokenPayload(
            Long userId,
            ClientInstanceId clientInstanceId,
            RoleType roleType
    ) {
        return createRefreshTokenPayload(userId, clientInstanceId, roleType, EXPIRE_AT);
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
