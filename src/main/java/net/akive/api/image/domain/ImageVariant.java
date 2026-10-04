package net.akive.api.image.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.image.domain.ImagePolicy.OBJECT_KEY_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class ImageVariant {

    private Long imageVariantId;

    private VariantType variantType;

    private String objectKey;

    private Integer recipeVersion;

    private Integer width;

    private Integer height;

    private ImageFormat format;

    private Long byteSize;

    public static ImageVariant reconstruct(
            Long imageVariantId,
            VariantType variantType,
            String objectKey,
            Integer recipeVersion,
            Integer width,
            Integer height,
            ImageFormat format,
            Long byteSize
    ) {
        return ImageVariant.builder()
                .imageVariantId(requirePositive(imageVariantId))
                .variantType(requireNonNull(variantType))
                .objectKey(requireMaxLength(requireNotBlank(objectKey), OBJECT_KEY_MAX_LENGTH))
                .recipeVersion(requirePositive(recipeVersion))
                .width(requirePositive(width))
                .height(requirePositive(height))
                .format(requireNonNull(format))
                .byteSize(requireNonNegativeIfPresent(byteSize))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageVariant that = (ImageVariant) o;

        return this.imageVariantId != null && Objects.equals(this.imageVariantId, that.imageVariantId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
