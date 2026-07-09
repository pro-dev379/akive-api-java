package net.akive.api.auth.domain.login;

import static net.akive.api.common.domain.DomainValidator.*;

public record LegacyUserPassword(
        Long userId,
        String hash,
        String salt
) {

    public LegacyUserPassword {
        requirePositive(userId);
        hash = requireNotBlank(hash);
        salt = requireNotBlank(salt);
    }
}
