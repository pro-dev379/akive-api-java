package net.akive.api.auth.domain.login;

import net.akive.api.user.domain.User;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record LegacyUserPassword(User user, LegacyPasswordHash passwordHash) {

    public LegacyUserPassword {
        requireNonNull(user);
        requireNonNull(passwordHash);
    }
}
