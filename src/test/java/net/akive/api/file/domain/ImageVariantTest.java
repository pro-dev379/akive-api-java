package net.akive.api.file.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.file.domain.FilePolicy.OBJECT_KEY_MAX_LENGTH;
import static net.akive.api.file.domain.ImageFormat.WEBP;
import static net.akive.api.file.domain.VariantType.THUMBNAIL;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ImageVariantTest {

    @Test
    void 저장된_이미지_변환본을_복원한다() {
        ImageVariant imageVariant = reconstructImageVariant(
                FIRST_IMAGE_VARIANT_ID,
                THUMBNAIL,
                IMAGE_VARIANT_OBJECT_KEY,
                RECIPE_VERSION,
                IMAGE_VARIANT_WIDTH,
                IMAGE_VARIANT_HEIGHT,
                WEBP,
                IMAGE_VARIANT_BYTE_SIZE
        );

        assertThat(imageVariant.getImageVariantId()).isEqualTo(FIRST_IMAGE_VARIANT_ID);
        assertThat(imageVariant.getVariantType()).isEqualTo(THUMBNAIL);
        assertThat(imageVariant.getObjectKey()).isEqualTo(IMAGE_VARIANT_OBJECT_KEY);
        assertThat(imageVariant.getRecipeVersion()).isEqualTo(RECIPE_VERSION);
        assertThat(imageVariant.getWidth()).isEqualTo(IMAGE_VARIANT_WIDTH);
        assertThat(imageVariant.getHeight()).isEqualTo(IMAGE_VARIANT_HEIGHT);
        assertThat(imageVariant.getFormat()).isEqualTo(WEBP);
        assertThat(imageVariant.getByteSize()).isEqualTo(IMAGE_VARIANT_BYTE_SIZE);
    }

    @Test
    void 객체_키가_공백이면_이미지_변환본을_복원할_수_없다() {
        assertViolation(() -> reconstructImageVariant(
                FIRST_IMAGE_VARIANT_ID,
                THUMBNAIL,
                " ",
                RECIPE_VERSION,
                IMAGE_VARIANT_WIDTH,
                IMAGE_VARIANT_HEIGHT,
                WEBP,
                IMAGE_VARIANT_BYTE_SIZE
        ), BLANK_VALUE);
    }

    @Test
    void 객체_키가_최대_길이를_초과하면_이미지_변환본을_복원할_수_없다() {
        assertViolation(() -> reconstructImageVariant(
                FIRST_IMAGE_VARIANT_ID,
                THUMBNAIL,
                "a".repeat(OBJECT_KEY_MAX_LENGTH + 1),
                RECIPE_VERSION,
                IMAGE_VARIANT_WIDTH,
                IMAGE_VARIANT_HEIGHT,
                WEBP,
                IMAGE_VARIANT_BYTE_SIZE
        ), MAX_LENGTH_EXCEEDED);
    }

    private static void assertViolation(
            ThrowableAssert.ThrowingCallable callable,
            BusinessRuleViolationError expectedError
    ) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(callable)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(expectedError);
    }
}
