package net.akive.api.auth.domain.token;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.domain.UserRole;

import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record RefreshTokenPayload(
        Long userId,
        ClientInstanceId clientInstanceId,
        UserRole role,
        Instant expiresAt
) {

    public RefreshTokenPayload {
        requirePositive(userId);
        requireNonNull(clientInstanceId);
        requireNonNull(role);
        requireNonNull(expiresAt);
    }
}
