package net.akive.api.image.adapter.webapi.mapper;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.CdnProperties;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.VariantType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ImageResponseMapper {

    private final CdnProperties cdnProperties;

    public Optional<URI> resolveUrl(ImageAsset imageAsset, VariantType variantType) {
        return imageAsset.findActiveVariant(variantType)
                .map(variant -> cdnProperties.resolve(variant.getObjectKey()));
    }
}
