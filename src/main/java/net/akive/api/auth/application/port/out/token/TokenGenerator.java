package net.akive.api.auth.application.port.out.token;

import net.akive.api.user.domain.RoleType;

public interface TokenGenerator {

    String createAccessToken(Long userid, RoleType roleType);
    String createRefreshToken(Long userId, RoleType roleType);
}
