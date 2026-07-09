package net.akive.api.auth.domain.token;

import net.akive.api.user.domain.RoleType;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.*;

public record TokenPayload(Long userId, RoleType roleType, Instant expiresAt) {

    public TokenPayload {
        requirePositive(userId);
        requireNonNull(roleType);
        requireNonNull(expiresAt);
    }
}
