package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.UserRole;

public interface RefreshTokenCreator {

    RefreshToken create(Long userid, ClientInstanceId clientInstanceId, UserRole role);
}
