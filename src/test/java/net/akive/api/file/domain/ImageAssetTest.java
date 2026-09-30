package net.akive.api.file.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.file.domain.FilePolicy.OBJECT_KEY_MAX_LENGTH;
import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.file.domain.ProcessingStatus.*;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.*;
import static net.akive.api.file.domain.fixture.ImageVariantFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ImageAssetTest {

    @Test
    void 저장된_이미지_에셋을_복원한다() {
        ImageVariant imageVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(imageVariant);

        assertThat(imageAsset.getProcessingStatus()).isEqualTo(READY);
        assertThat(imageAsset.getActiveRecipeVersion()).isEqualTo(RECIPE_VERSION);
        assertThat(imageAsset.getProcessedAt()).isEqualTo(PROCESSED_AT);
        assertThat(imageAsset.getVariants()).containsExactly(imageVariant);
        assertThat(imageAsset.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 이미지_업로드_대기_상태에서_변환본이_있으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructImageAsset(
                MASTER_OBJECT_KEY,
                null,
                UPLOADING,
                null,
                null,
                List.of()
        ));
    }

    @Test
    void 이미지_변환_상태에서_마스터_객체_키와_부분_생성된_변환본이_존재할_수_있다() {
        ImageVariant imageVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);

        ImageAsset imageAsset = reconstructImageAsset(
                MASTER_OBJECT_KEY,
                null,
                PROCESSING,
                null,
                null,
                List.of(imageVariant)
        );

        assertThat(imageAsset.getProcessingStatus()).isEqualTo(PROCESSING);
        assertThat(imageAsset.getMasterObjectKey()).isEqualTo(MASTER_OBJECT_KEY);
        assertThat(imageAsset.getVariants()).containsExactly(imageVariant);
    }

    @Test
    void 이미지_변환_중_상태에서_마스터_객체_키가_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructImageAsset(
                null,
                null,
                PROCESSING,
                null,
                null,
                List.of()
        ));
    }

    @Test
    void 이미지_변환_완료_상태에서_활성화된_변환_규칙_버전과_변환본이_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructImageAsset(
                MASTER_OBJECT_KEY,
                null,
                READY,
                null,
                PROCESSED_AT,
                List.of()
        ));
    }

    @Test
    void 이미지_변환_실패_상태에서_오류_메시지가_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructImageAsset(
                MASTER_OBJECT_KEY,
                null,
                FAILED,
                " ",
                null,
                List.of()
        ));
    }

    @Test
    void 변환_규칙_버전과_변환본_유형이_중복된_변환본이_있으면_복원할_수_없다() {
        assertInvalidState(() -> readyImageAsset(
                thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION),
                thumbnailImageVariant(SECOND_IMAGE_VARIANT_ID, RECIPE_VERSION)
        ));
    }

    @Test
    void 마스터_객체_키가_공백이면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructImageAsset(
                        " ",
                        null,
                        PROCESSING,
                        null,
                        null,
                        List.of()
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 마스터_객체_키가_512자를_초과하면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructImageAsset(
                        "a".repeat(OBJECT_KEY_MAX_LENGTH + 1),
                        null,
                        PROCESSING,
                        null,
                        null,
                        List.of()
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 이미지_에셋을_생성한다() {
        ImageAsset imageAsset = ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF);

        assertThat(imageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(imageAsset.getImageType()).isEqualTo(ATTENDANCE_PROOF);
        assertThat(imageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(imageAsset.getMasterObjectKey()).isNull();
        assertThat(imageAsset.getActiveRecipeVersion()).isNull();
        assertThat(imageAsset.getProcessingError()).isNull();
        assertThat(imageAsset.getProcessedAt()).isNull();
        assertThat(imageAsset.getVariants()).isEmpty();
        assertThat(imageAsset.getCreatedAt()).isNull();
    }

    private static void assertInvalidState(ThrowableAssert.ThrowingCallable callable) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(callable)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_IMAGE_ASSET_STATE);
    }
}
