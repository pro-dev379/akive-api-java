package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.image.application.provided.ImageAssetUploadChecker;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.domain.ImageType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class ImageAssetUploadCheckService implements ImageAssetUploadChecker {

    private final ImageStorage imageStorage;

    @Override
    public boolean isOriginalImageUploaded(UUID imageAssetId, ImageType imageType) {
        return imageStorage.existsOriginalImage(imageAssetId, imageType);
    }
}
