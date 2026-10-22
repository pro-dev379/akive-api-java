package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.image.application.provided.ImageAssetReader;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ImageAssetReadService implements ImageAssetReader {

    private final ImageAssetRepository imageAssetRepository;

    @Override
    public Optional<ImageAsset> find(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId);
    }

    @Override
    @Transactional
    public Optional<ImageAsset> findForUpdate(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetIdForUpdate(imageAssetId);
    }
}
