package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.file.application.provided.ImageAssetDeleter;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class ImageAssetDeleteService implements ImageAssetDeleter {

    private final FileStorage fileStorage;
    private final ImageAssetRepository imageAssetRepository;

    @Override
    public void delete(UUID imageAssetId) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);

        if (imageAsset.getMasterObjectKey() != null) {
            fileStorage.delete(imageAsset.getMasterObjectKey());
        }
        imageAsset.getVariants().forEach(variant -> fileStorage.delete(variant.getObjectKey()));

        imageAssetRepository.delete(imageAsset);
    }

    private ImageAsset findImageAssetOrThrow(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }
}
