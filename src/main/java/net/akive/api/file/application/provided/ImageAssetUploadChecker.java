package net.akive.api.file.application.provided;

import net.akive.api.file.domain.ImageType;

import java.util.UUID;

public interface ImageAssetUploadChecker {

    boolean isOriginalUploaded(UUID imageAssetId, ImageType imageType);
}
