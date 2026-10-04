package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.image.application.provided.ImageAssetDeleter;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.domain.ImageAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class ImageAssetDeleteService implements ImageAssetDeleter {

    private final ImageStorage imageStorage;
    private final ImageAssetRepository imageAssetRepository;

    @Override
    public void delete(UUID imageAssetId) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);

        if (imageAsset.getMasterObjectKey() != null) {
            imageStorage.delete(imageAsset.getMasterObjectKey());
        }
        imageAsset.getVariants().forEach(variant -> imageStorage.delete(variant.getObjectKey()));

        imageAssetRepository.delete(imageAsset);
    }

    private ImageAsset findImageAssetOrThrow(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }
}
