package net.akive.api.file.domain.fixture;

import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.ImageFormat;
import net.akive.api.file.domain.ImageVariant;
import net.akive.api.file.domain.ProcessingStatus;
import net.akive.api.file.domain.VariantType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.akive.api.file.domain.ImageFormat.WEBP;
import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;

public class ImageAssetFixture {

    public static final UUID IMAGE_ASSET_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdef");
    public static final String MASTER_OBJECT_KEY =
            "uploads/attendance-proofs/0199a8e2-7c31-7abc-8123-456789abcdef/original.jpg";
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 9, 30, 0, 0);
    public static final LocalDateTime PROCESSED_AT = CREATED_AT.plusMinutes(1);
    public static final Long FIRST_IMAGE_VARIANT_ID = 1L;
    public static final Long SECOND_IMAGE_VARIANT_ID = 2L;
    public static final Integer RECIPE_VERSION = 1;
    public static final String IMAGE_VARIANT_OBJECT_KEY = "variants/attendance-proofs/1/thumbnail.webp";
    public static final Integer IMAGE_VARIANT_WIDTH = 500;
    public static final Integer IMAGE_VARIANT_HEIGHT = 500;
    public static final Long IMAGE_VARIANT_BYTE_SIZE = 10_000L;

    private ImageAssetFixture() {
    }

    public static ImageAsset reconstructImageAsset(
            String masterObjectKey,
            Integer activeRecipeVersion,
            ProcessingStatus processingStatus,
            String processingError,
            LocalDateTime processedAt,
            List<ImageVariant> variants
    ) {
        return ImageAsset.reconstruct(
                IMAGE_ASSET_ID,
                ATTENDANCE_PROOF,
                masterObjectKey,
                activeRecipeVersion,
                processingStatus,
                processingError,
                processedAt,
                variants,
                CREATED_AT
        );
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
}
