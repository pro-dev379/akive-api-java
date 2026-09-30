package net.akive.api.file.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.core.config.file.CdnProperties;
import net.akive.api.file.application.fixture.FileCommandFixture;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.application.required.ImageAssetIdGenerator;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.ImageType;
import net.akive.api.file.domain.ImageVariant;
import net.akive.api.file.domain.UploadUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.core.config.file.fixture.CdnFixture.CDN_BASE_URL;
import static net.akive.api.file.adapter.storage.fixture.S3FileStorageFixture.EXPIRES_AT;
import static net.akive.api.file.adapter.storage.fixture.S3FileStorageFixture.UPLOAD_URL;
import static net.akive.api.file.application.fixture.FileCommandFixture.FILE_SIZE;
import static net.akive.api.file.application.fixture.FileCommandFixture.createUploadUrlCreateCommand;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.file.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.file.domain.VariantType.DETAIL;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.*;
import static net.akive.api.file.domain.fixture.ImageVariantFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    FileStorage fileStorage;

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    ImageAssetIdGenerator imageAssetIdGenerator;

    @Mock
    CdnProperties cdnProperties;

    @InjectMocks
    FileService fileService;

    @ParameterizedTest
    @MethodSource("allowedUploadFiles")
    void 이미지_타입과_콘텐츠_타입에_맞는_파일_키로_업로드_URL을_생성한다(
            ImageType imageType,
            String contentType,
            String imageTypePath,
            String extension,
            long fileSize
    ) {
        UploadUrlCreateCommand command = createUploadUrlCreateCommand(imageType, contentType, fileSize);
        given(imageAssetIdGenerator.generate()).willReturn(IMAGE_ASSET_ID);
        given(imageAssetRepository.save(any(ImageAsset.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(fileStorage.createUploadUrl(
                anyString(),
                eq(contentType),
                eq(fileSize),
                eq(UPLOAD_URL_EXPIRATION_DURATION)
        )).willReturn(new UploadUrl(UPLOAD_URL, EXPIRES_AT));

        UploadUrlCreateResult result = fileService.createUploadUrl(command);

        ArgumentCaptor<ImageAsset> imageAssetCaptor = ArgumentCaptor.forClass(ImageAsset.class);
        then(imageAssetRepository).should().save(imageAssetCaptor.capture());

        ImageAsset savedImageAsset = imageAssetCaptor.getValue();
        assertThat(savedImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(savedImageAsset.getImageType()).isEqualTo(imageType);
        assertThat(savedImageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(savedImageAsset.getVariants()).isEmpty();
        assertThat(savedImageAsset.getCreatedAt()).isNull();

        ArgumentCaptor<String> fileKeyCaptor = ArgumentCaptor.forClass(String.class);
        then(fileStorage).should().createUploadUrl(
                fileKeyCaptor.capture(),
                eq(contentType),
                eq(fileSize),
                eq(UPLOAD_URL_EXPIRATION_DURATION)
        );

        String fileKey = fileKeyCaptor.getValue();
        assertThat(fileKey)
                .isEqualTo("uploads/%s/%s/original.%s".formatted(imageTypePath, IMAGE_ASSET_ID, extension));
        assertThat(result.imageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(result.uploadUrl()).isEqualTo(UPLOAD_URL);
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @ParameterizedTest
    @MethodSource("oversizedUploadFiles")
    void 이미지_타입별_최대_파일_크기를_초과하면_업로드_URL을_생성할_수_없다(ImageType imageType, long fileSize) {
        UploadUrlCreateCommand command =
                createUploadUrlCreateCommand(imageType, "image/jpeg", fileSize);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> fileService.createUploadUrl(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(FILE_SIZE_EXCEEDED);

        then(imageAssetIdGenerator).shouldHaveNoInteractions();
        then(imageAssetRepository).shouldHaveNoInteractions();
        then(fileStorage).shouldHaveNoInteractions();
    }

    @Test
    void 허용되지_않은_콘텐츠_타입이면_업로드_URL을_생성할_수_없다() {
        UploadUrlCreateCommand command = createUploadUrlCreateCommand(
                PROFILE_IMAGE,
                "image/svg+xml",
                FILE_SIZE
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> fileService.createUploadUrl(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(FILE_CONTENT_TYPE_NOT_ALLOWED);

        then(imageAssetIdGenerator).shouldHaveNoInteractions();
        then(imageAssetRepository).shouldHaveNoInteractions();
        then(fileStorage).shouldHaveNoInteractions();
    }

    @Test
    void 이미지_URL을_조회한다() {
        ImageVariant activeImageVariant = detailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageVariant inactiveImageVariant = detailImageVariant(SECOND_IMAGE_VARIANT_ID, ANOTHER_RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(activeImageVariant, inactiveImageVariant);
        URI expectedUrl = cdnUrlOf(activeImageVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));
        given(cdnProperties.resolve(activeImageVariant.getObjectKey())).willReturn(expectedUrl);

        Optional<URI> result = fileService.findImageUrl(IMAGE_ASSET_ID, DETAIL);

        assertThat(result).contains(expectedUrl);
        then(cdnProperties).should().resolve(activeImageVariant.getObjectKey());
        then(cdnProperties).shouldHaveNoMoreInteractions();
    }

    @Test
    void 활성화된_변환_규칙_버전의_변환본이_없으면_이미지_URL을_조회할_수_없다() {
        ImageVariant activeThumbnailVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(activeThumbnailVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        Optional<URI> result = fileService.findImageUrl(IMAGE_ASSET_ID, DETAIL);

        assertThat(result).isEmpty();
        then(cdnProperties).shouldHaveNoInteractions();
    }

    @Test
    void 이미지_에셋을_삭제한다() {
        ImageVariant thumbnailVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageVariant detailVariant = detailImageVariant(SECOND_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(thumbnailVariant, detailVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        fileService.deleteImageAsset(IMAGE_ASSET_ID);

        then(fileStorage).should().delete(MASTER_OBJECT_KEY);
        then(fileStorage).should().delete(thumbnailVariant.getObjectKey());
        then(fileStorage).should().delete(detailVariant.getObjectKey());
        then(imageAssetRepository).should().delete(imageAsset);
    }

    @Test
    void 존재하지_않는_이미지_에셋은_삭제할_수_없다() {
        given(imageAssetRepository.findByImageAssetId(UNKNOWN_IMAGE_ASSET_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> fileService.deleteImageAsset(UNKNOWN_IMAGE_ASSET_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(IMAGE_ASSET_NOT_FOUND);

        then(imageAssetRepository).should().findByImageAssetId(UNKNOWN_IMAGE_ASSET_ID);
        then(fileStorage).shouldHaveNoInteractions();
        then(imageAssetRepository).should(never()).delete(any(ImageAsset.class));
    }

    private static Stream<Arguments> allowedUploadFiles() {
        return FileCommandFixture.allowedUploadFiles();
    }

    private static Stream<Arguments> oversizedUploadFiles() {
        return FileCommandFixture.oversizedUploadFiles();
    }

    private static URI cdnUrlOf(ImageVariant variant) {
        return URI.create(CDN_BASE_URL + "/" + variant.getObjectKey());
    }
}
