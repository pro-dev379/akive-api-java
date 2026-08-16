package net.akive.api.auth.domain.login;

import static net.akive.api.common.domain.DomainValidator.*;

public record LegacyUserPassword(Long userId, LegacyPasswordHash passwordHash) {

    public LegacyUserPassword {
        requirePositive(userId);
        requireNonNull(passwordHash);
    }
}
