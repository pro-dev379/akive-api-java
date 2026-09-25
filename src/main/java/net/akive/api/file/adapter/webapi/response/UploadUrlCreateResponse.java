package net.akive.api.file.adapter.webapi.response;

import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;

import java.time.Instant;

public record UploadUrlCreateResponse(String fileKey, String uploadUrl, Instant expiresAt) {

    public static UploadUrlCreateResponse from(UploadUrlCreateResult result) {
        return new UploadUrlCreateResponse(
                result.fileKey(),
                result.uploadUrl().toString(),
                result.expiresAt()
        );
    }
}
