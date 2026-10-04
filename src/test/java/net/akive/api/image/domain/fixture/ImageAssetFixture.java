package net.akive.api.image.domain.fixture;

import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageVariant;
import net.akive.api.image.domain.ProcessingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.ProcessingStatus.READY;

public class ImageAssetFixture {

    public static final UUID IMAGE_ASSET_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdef");
    public static final UUID ANOTHER_IMAGE_ASSET_ID = UUID.fromString("08609705-5187-4956-872d-e78834230681");
    public static final UUID UNKNOWN_IMAGE_ASSET_ID = UUID.fromString("d032ed06-de8f-4f49-9489-535973ed3126");
    public static final String MASTER_OBJECT_KEY =
            "uploads/attendance-proofs/0199a8e2-7c31-7abc-8123-456789abcdef/original.jpg";
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 9, 30, 0, 0);
    public static final LocalDateTime PROCESSED_AT = CREATED_AT.plusMinutes(1);
    public static final Integer RECIPE_VERSION = 1;
    public static final Integer ANOTHER_RECIPE_VERSION = 2;

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

    public static ImageAsset readyImageAsset(ImageVariant... variants) {
        return reconstructImageAsset(
                MASTER_OBJECT_KEY,
                RECIPE_VERSION,
                READY,
                null,
                PROCESSED_AT,
                List.of(variants)
        );
    }
}
