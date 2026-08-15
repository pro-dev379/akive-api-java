package net.akive.api.auth.domain.token;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record AccessToken(String value) {

    public AccessToken {
        requireNotBlank(value);
    }
}
