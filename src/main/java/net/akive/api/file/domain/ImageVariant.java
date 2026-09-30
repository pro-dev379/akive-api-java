package net.akive.api.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.file.domain.FilePolicy.OBJECT_KEY_MAX_LENGTH;

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
}
