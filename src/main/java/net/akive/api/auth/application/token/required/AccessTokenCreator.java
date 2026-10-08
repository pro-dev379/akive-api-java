package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.AccessToken;
import net.akive.api.user.domain.UserRole;

public interface AccessTokenCreator {

    AccessToken create(Long userId, UserRole role);
}
