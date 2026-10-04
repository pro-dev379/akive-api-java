package net.akive.api.image.application.required;

import net.akive.api.image.domain.ImageAsset;

import java.util.Optional;
import java.util.UUID;

public interface ImageAssetRepository {

    ImageAsset save(ImageAsset imageAsset);

    Optional<ImageAsset> findByImageAssetId(UUID imageAssetId);

    void delete(ImageAsset imageAsset);
}
