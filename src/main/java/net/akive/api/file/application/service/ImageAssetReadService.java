package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.core.config.file.CdnProperties;
import net.akive.api.file.application.provided.ImageAssetReader;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.VariantType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ImageAssetReadService implements ImageAssetReader {

    private final ImageAssetRepository imageAssetRepository;
    private final CdnProperties cdnProperties;

    @Override
    @Transactional(readOnly = true)
    public Optional<URI> findImageUrl(UUID imageAssetId, VariantType variantType) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);

        return imageAsset.getVariants().stream()
                .filter(variant -> variant.getRecipeVersion().equals(imageAsset.getActiveRecipeVersion()))
                .filter(variant -> variant.getVariantType() == variantType)
                .findFirst()
                .map(variant -> cdnProperties.resolve(variant.getObjectKey()));
    }

    private ImageAsset findImageAssetOrThrow(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }
}
