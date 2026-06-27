package net.akive.api.auth.domain.login;

import net.akive.api.user.domain.RoleType;

public record LoginUser(
        Long userId,
        String email,
        String password,
        RoleType roleType
) {
}
