package net.akive.api.file.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.VariantType;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface ImageAssetReader {

    Optional<ImageAsset> find(UUID imageAssetId);

    Optional<URI> findImageUrl(UUID imageAssetId, VariantType variantType) throws ResourceNotFoundException;
}
