package net.akive.api.file.application.required;

import net.akive.api.file.domain.ImageAsset;

import java.util.Optional;
import java.util.UUID;

public interface ImageAssetRepository {

    ImageAsset save(ImageAsset imageAsset);

    Optional<ImageAsset> findByImageAssetId(UUID imageAssetId);

    void delete(ImageAsset imageAsset);
}
