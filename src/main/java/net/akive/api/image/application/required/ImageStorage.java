package net.akive.api.image.application.required;

import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.UploadUrl;

import java.time.Duration;
import java.util.UUID;

public interface ImageStorage {

    UploadUrl createOriginalImageUploadUrl(
            UUID imageAssetId,
            ImageType imageType,
            String extension,
            String contentType,
            long fileSize,
            Duration expiration
    );

    boolean existsOriginalImage(UUID imageAssetId, ImageType imageType);

    void delete(String objectKey);
}
