package net.akive.api.file.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.application.fixture.FileCommandFixture;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.application.required.ImageAssetIdGenerator;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.ImageType;
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

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;
import static net.akive.api.file.application.fixture.FileCommandFixture.FILE_SIZE;
import static net.akive.api.file.application.fixture.FileCommandFixture.createUploadUrlCreateCommand;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.file.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.file.domain.fixture.FileFixture.EXPIRES_AT;
import static net.akive.api.file.domain.fixture.FileFixture.UPLOAD_URL;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    FileStorage fileStorage;

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    ImageAssetIdGenerator imageAssetIdGenerator;

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
        assertThat(result.fileKey()).isEqualTo(fileKey);
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

    private static Stream<Arguments> allowedUploadFiles() {
        return FileCommandFixture.allowedUploadFiles();
    }

    private static Stream<Arguments> oversizedUploadFiles() {
        return FileCommandFixture.oversizedUploadFiles();
    }
}
