package net.akive.api.auth.domain.token;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record RefreshToken(String value) {

    public RefreshToken {
        requireNotBlank(value);
    }
}
