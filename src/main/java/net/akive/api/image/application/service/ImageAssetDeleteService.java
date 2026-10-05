package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.image.application.event.ImageAssetDeletedEvent;
import net.akive.api.image.application.provided.ImageAssetDeleter;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class ImageAssetDeleteService implements ImageAssetDeleter {

    private final ImageAssetRepository imageAssetRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void delete(UUID imageAssetId) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);
        List<String> objectKeys = collectObjectKeys(imageAsset);

        imageAssetRepository.delete(imageAsset);
        applicationEventPublisher.publishEvent(new ImageAssetDeletedEvent(objectKeys));
    }

    private ImageAsset findImageAssetOrThrow(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }

    private List<String> collectObjectKeys(ImageAsset imageAsset) {
        List<String> objectKeys = new ArrayList<>();

        if (imageAsset.getMasterObjectKey() != null) {
            objectKeys.add(imageAsset.getMasterObjectKey());
        }
        imageAsset.getVariants().forEach(variant -> objectKeys.add(variant.getObjectKey()));

        return objectKeys;
    }
}
