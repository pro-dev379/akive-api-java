package net.akive.api.image.application.provided;

import net.akive.api.image.domain.ImageType;

import java.util.UUID;

public interface ImageAssetUploadChecker {

    boolean isOriginalImageUploaded(UUID imageAssetId, ImageType imageType);
}
