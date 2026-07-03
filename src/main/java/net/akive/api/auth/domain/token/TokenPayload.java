package net.akive.api.auth.domain.token;

import net.akive.api.user.domain.RoleType;

import java.time.Instant;

public record TokenPayload(Long userId, RoleType roleType, Instant expiresAt) {
}
