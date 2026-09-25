package net.akive.api.file.application.provided.dto;

import java.net.URI;
import java.time.Instant;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record UploadUrlCreateResult(
        String fileKey,
        URI uploadUrl,
        Instant expiresAt
) {

    public UploadUrlCreateResult {
        requireNotBlank(fileKey);
        requireNonNull(uploadUrl);
        requireNonNull(expiresAt);
    }
}
