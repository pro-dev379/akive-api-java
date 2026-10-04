package net.akive.api.image.domain;

import java.net.URI;
import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record UploadUrl(URI value, Instant expiresAt) {

    public UploadUrl {
        requireNonNull(value);
        requireNonNull(expiresAt);
    }
}
