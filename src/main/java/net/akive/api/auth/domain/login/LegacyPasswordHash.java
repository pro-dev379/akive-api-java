package net.akive.api.auth.domain.login;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record LegacyPasswordHash(String hash, String salt) {

    public LegacyPasswordHash {
        requireNotBlank(hash);
        requireNotBlank(salt);
    }
}
