package net.akive.api.file.adapter.webapi.response;

import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;

import java.time.Instant;
import java.util.UUID;

public record UploadUrlCreateResponse(UUID imageAssetId, String uploadUrl, Instant expiresAt) {

    public static UploadUrlCreateResponse from(UploadUrlCreateResult result) {
        return new UploadUrlCreateResponse(
                result.imageAssetId(),
                result.uploadUrl().toString(),
                result.expiresAt()
        );
    }
}
