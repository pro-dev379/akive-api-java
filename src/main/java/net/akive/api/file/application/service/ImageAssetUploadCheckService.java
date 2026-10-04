package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.file.application.provided.ImageAssetUploadChecker;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.domain.ImageType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class ImageAssetUploadCheckService implements ImageAssetUploadChecker {

    private final FileStorage fileStorage;

    @Override
    public boolean isOriginalUploaded(UUID imageAssetId, ImageType imageType) {
        return fileStorage.existsOriginalImage(imageAssetId, imageType);
    }
}
