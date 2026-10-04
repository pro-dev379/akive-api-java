package net.akive.api.file.application.required;

import net.akive.api.file.domain.ImageType;
import net.akive.api.file.domain.UploadUrl;

import java.time.Duration;
import java.util.UUID;

public interface FileStorage {

    UploadUrl createOriginalImageUploadUrl(
            UUID imageAssetId,
            ImageType imageType,
            String extension,
            String contentType,
            long fileSize,
            Duration expiration
    );

    boolean existsOriginalImage(UUID imageAssetId, ImageType imageType);

    void delete(String fileKey);
}
