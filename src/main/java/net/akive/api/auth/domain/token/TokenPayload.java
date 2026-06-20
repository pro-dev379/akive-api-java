package net.akive.api.auth.domain.token;

import net.akive.api.user.domain.RoleType;

public record TokenPayload(Long userId, RoleType roleType) {
}
