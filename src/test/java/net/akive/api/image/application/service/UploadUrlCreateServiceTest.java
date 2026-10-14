package net.akive.api.image.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.id.UuidGenerator;
import net.akive.api.image.application.fixture.ImageCommandFixture;
import net.akive.api.image.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.image.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.UploadUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IMAGE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.IMAGE_FILE_SIZE_EXCEEDED;
import static net.akive.api.image.adapter.storage.fixture.S3ImageStorageFixture.EXPIRES_AT;
import static net.akive.api.image.adapter.storage.fixture.S3ImageStorageFixture.UPLOAD_URL;
import static net.akive.api.image.application.fixture.ImageCommandFixture.IMAGE_FILE_SIZE;
import static net.akive.api.image.application.fixture.ImageCommandFixture.createUploadUrlCreateCommand;
import static net.akive.api.image.domain.ImagePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UploadUrlCreateServiceTest {

    @Mock
    ImageStorage imageStorage;

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    UuidGenerator uuidGenerator;

    @InjectMocks
    UploadUrlCreateService uploadUrlCreateService;

    @ParameterizedTest
    @MethodSource("supportedImageUploadArguments")
    void 이미지_타입과_콘텐츠_타입에_맞는_오브젝트_키로_업로드_URL을_생성한다(
            ImageType imageType,
            String contentType,
            String extension,
            long fileSize
    ) {
        UploadUrlCreateCommand command = createUploadUrlCreateCommand(imageType, contentType, fileSize);
        given(uuidGenerator.generate()).willReturn(IMAGE_ASSET_ID);
        given(imageAssetRepository.save(any(ImageAsset.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(imageStorage.createOriginalImageUploadUrl(
                eq(IMAGE_ASSET_ID),
                eq(imageType),
                eq(extension),
                eq(contentType),
                eq(fileSize),
                eq(UPLOAD_URL_EXPIRATION_DURATION)
        )).willReturn(new UploadUrl(UPLOAD_URL, EXPIRES_AT));

        UploadUrlCreateResult result = uploadUrlCreateService.create(command);

        ArgumentCaptor<ImageAsset> imageAssetCaptor = ArgumentCaptor.forClass(ImageAsset.class);
        then(imageAssetRepository).should().save(imageAssetCaptor.capture());

        ImageAsset savedImageAsset = imageAssetCaptor.getValue();
        assertThat(savedImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(savedImageAsset.getImageType()).isEqualTo(imageType);
        assertThat(savedImageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(savedImageAsset.getVariants()).isEmpty();
        assertThat(savedImageAsset.getCreatedAt()).isNull();

        then(imageStorage).should().createOriginalImageUploadUrl(
                IMAGE_ASSET_ID,
                imageType,
                extension,
                contentType,
                fileSize,
                UPLOAD_URL_EXPIRATION_DURATION
        );

        assertThat(result.imageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(result.uploadUrl()).isEqualTo(UPLOAD_URL);
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @ParameterizedTest
    @MethodSource("oversizedImageUploadArguments")
    void 이미지_타입별_최대_파일_사이즈를_초과하면_업로드_URL을_생성할_수_없다(ImageType imageType, long fileSize) {
        UploadUrlCreateCommand command =
                createUploadUrlCreateCommand(imageType, "image/jpeg", fileSize);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> uploadUrlCreateService.create(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(IMAGE_FILE_SIZE_EXCEEDED);

        then(uuidGenerator).shouldHaveNoInteractions();
        then(imageAssetRepository).shouldHaveNoInteractions();
        then(imageStorage).shouldHaveNoInteractions();
    }

    @Test
    void 허용되지_않은_콘텐츠_타입이면_업로드_URL을_생성할_수_없다() {
        UploadUrlCreateCommand command = createUploadUrlCreateCommand(
                PROFILE_IMAGE,
                "image/svg+xml",
                IMAGE_FILE_SIZE
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> uploadUrlCreateService.create(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(IMAGE_CONTENT_TYPE_NOT_ALLOWED);

        then(uuidGenerator).shouldHaveNoInteractions();
        then(imageAssetRepository).shouldHaveNoInteractions();
        then(imageStorage).shouldHaveNoInteractions();
    }

    private static Stream<Arguments> supportedImageUploadArguments() {
        return ImageCommandFixture.supportedImageUploadArguments();
    }

    private static Stream<Arguments> oversizedImageUploadArguments() {
        return ImageCommandFixture.oversizedImageUploadArguments();
    }
}
