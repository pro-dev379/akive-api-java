package net.akive.api.image.application.provided;

import net.akive.api.image.domain.ImageAsset;

import java.util.Optional;
import java.util.UUID;

public interface ImageAssetReader {

    Optional<ImageAsset> find(UUID imageAssetId);
}
