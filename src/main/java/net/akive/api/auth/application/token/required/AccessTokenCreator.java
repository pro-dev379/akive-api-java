package net.akive.api.auth.application.token.required;

import net.akive.api.auth.domain.token.AccessToken;
import net.akive.api.user.domain.RoleType;

public interface AccessTokenCreator {

    AccessToken create(Long userid, RoleType roleType);
}
