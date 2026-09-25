package net.akive.api.file.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.application.fixture.FileCommandFixture;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.domain.UploadPurpose;
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
import static net.akive.api.file.domain.FileUploadPolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.UploadPurpose.PROFILE_IMAGE;
import static net.akive.api.file.domain.fixture.FileFixture.EXPIRES_AT;
import static net.akive.api.file.domain.fixture.FileFixture.UPLOAD_URL;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    FileStorage fileStorage;

    @InjectMocks
    FileService fileService;

    @ParameterizedTest
    @MethodSource("allowedUploadFiles")
    void 업로드_목적과_콘텐츠_타입에_맞는_파일_키로_업로드_URL을_생성한다(
            UploadPurpose uploadPurpose,
            String contentType,
            String directory,
            String extension,
            long fileSize
    ) {
        UploadUrlCreateCommand command = createUploadUrlCreateCommand(uploadPurpose, contentType, fileSize);
        given(fileStorage.createUploadUrl(
                anyString(),
                eq(contentType),
                eq(fileSize),
                eq(UPLOAD_URL_EXPIRATION_DURATION)
        )).willReturn(new UploadUrl(UPLOAD_URL, EXPIRES_AT));

        UploadUrlCreateResult result = fileService.createUploadUrl(command);

        ArgumentCaptor<String> fileKeyCaptor = ArgumentCaptor.forClass(String.class);
        then(fileStorage).should().createUploadUrl(
                fileKeyCaptor.capture(),
                eq(contentType),
                eq(fileSize),
                eq(UPLOAD_URL_EXPIRATION_DURATION)
        );

        String fileKey = fileKeyCaptor.getValue();
        assertThat(fileKey)
                .startsWith(directory + "/" + USER_ID + "/")
                .endsWith("." + extension);
        assertThat(result.fileKey()).isEqualTo(fileKey);
        assertThat(result.uploadUrl()).isEqualTo(UPLOAD_URL);
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @ParameterizedTest
    @MethodSource("oversizedUploadFiles")
    void 업로드_목적별_최대_파일_크기를_초과하면_업로드_URL을_생성할_수_없다(UploadPurpose uploadPurpose, long fileSize) {
        UploadUrlCreateCommand command =
                createUploadUrlCreateCommand(uploadPurpose, "image/jpeg", fileSize);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> fileService.createUploadUrl(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(FILE_SIZE_EXCEEDED);

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

        then(fileStorage).shouldHaveNoInteractions();
    }

    private static Stream<Arguments> allowedUploadFiles() {
        return FileCommandFixture.allowedUploadFiles();
    }

    private static Stream<Arguments> oversizedUploadFiles() {
        return FileCommandFixture.oversizedUploadFiles();
    }
}
