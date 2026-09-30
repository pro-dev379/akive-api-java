package net.akive.api.file.application.provided.dto;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record UploadUrlCreateResult(
        UUID imageAssetId,
        URI uploadUrl,
        Instant expiresAt
) {

    public UploadUrlCreateResult {
        requireNonNull(imageAssetId);
        requireNonNull(uploadUrl);
        requireNonNull(expiresAt);
    }
}
