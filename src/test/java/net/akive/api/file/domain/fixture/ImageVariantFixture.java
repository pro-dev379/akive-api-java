package net.akive.api.file.domain.fixture;

import net.akive.api.file.domain.ImageFormat;
import net.akive.api.file.domain.ImageVariant;
import net.akive.api.file.domain.VariantType;

import static net.akive.api.file.domain.ImageFormat.WEBP;
import static net.akive.api.file.domain.VariantType.DETAIL;
import static net.akive.api.file.domain.VariantType.THUMBNAIL;

public class ImageVariantFixture {

    public static final Long FIRST_IMAGE_VARIANT_ID = 1L;
    public static final Long SECOND_IMAGE_VARIANT_ID = 2L;
    public static final String IMAGE_VARIANT_OBJECT_KEY = "variants/attendance-proofs/1/thumbnail.webp";
    public static final Integer IMAGE_VARIANT_WIDTH = 500;
    public static final Integer IMAGE_VARIANT_HEIGHT = 500;
    public static final Long IMAGE_VARIANT_BYTE_SIZE = 10_000L;

    private ImageVariantFixture() {
    }

    public static ImageVariant reconstructImageVariant(
            Long imageVariantId,
            int recipeVersion,
            VariantType variantType
    ) {
        return reconstructImageVariant(
                imageVariantId,
                variantType,
                "variants/attendance-proofs/%d/%s.webp".formatted(recipeVersion, variantType.name().toLowerCase()),
                recipeVersion,
                IMAGE_VARIANT_WIDTH,
                IMAGE_VARIANT_HEIGHT,
                WEBP,
                IMAGE_VARIANT_BYTE_SIZE
        );
    }

    public static ImageVariant reconstructImageVariant(
            Long imageVariantId,
            VariantType variantType,
            String objectKey,
            Integer recipeVersion,
            Integer width,
            Integer height,
            ImageFormat format,
            Long byteSize
    ) {
        return ImageVariant.reconstruct(
                imageVariantId,
                variantType,
                objectKey,
                recipeVersion,
                width,
                height,
                format,
                byteSize
        );
    }

    public static ImageVariant detailImageVariant(Long imageVariantId, int recipeVersion) {
        return reconstructImageVariant(imageVariantId, recipeVersion, DETAIL);
    }

    public static ImageVariant thumbnailImageVariant(Long imageVariantId, int recipeVersion) {
        return reconstructImageVariant(imageVariantId, recipeVersion, THUMBNAIL);
    }
}
