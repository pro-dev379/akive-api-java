package net.akive.api.file.application.required;

import net.akive.api.file.domain.ImageAsset;

public interface ImageAssetRepository {

    ImageAsset save(ImageAsset imageAsset);
}
