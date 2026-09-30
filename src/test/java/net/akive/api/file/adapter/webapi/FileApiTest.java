package net.akive.api.file.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.adapter.webapi.fixture.FileRequestFixture;
import net.akive.api.file.adapter.webapi.request.UploadUrlCreateRequest;
import net.akive.api.file.application.provided.FileManager;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_SIZE_EXCEEDED;
import static net.akive.api.file.adapter.webapi.fixture.FileRequestFixture.createUploadUrlCreateRequest;
import static net.akive.api.file.domain.FilePolicy.PROFILE_IMAGE_MAX_SIZE_BYTES;
import static net.akive.api.file.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.file.domain.fixture.FileFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(FileApi.class)
class FileApiTest extends PrivateApiTestSupport {

    private static final String UPLOAD_URLS_URL = "/files/upload-urls";

    @MockitoBean
    FileManager fileManager;

    @Test
    void 파일_업로드_URL을_생성한다() {
        UploadUrlCreateRequest request = createUploadUrlCreateRequest();
        UploadUrlCreateCommand command = request.to(USER_ID);
        UploadUrlCreateResult result = new UploadUrlCreateResult(FILE_KEY, UPLOAD_URL, EXPIRES_AT);
        given(fileManager.createUploadUrl(command)).willReturn(result);

        MvcTestResultAssert response = postJson(UPLOAD_URLS_URL, USER_ID, toJson(request)).hasStatus(OK);

        response.bodyJson().extractingPath("$.fileKey").isEqualTo(FILE_KEY);
        response.bodyJson().extractingPath("$.uploadUrl").isEqualTo(UPLOAD_URL.toString());
        response.bodyJson().extractingPath("$.expiresAt").isEqualTo(EXPIRES_AT.toString());
        then(fileManager).should().createUploadUrl(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUploadUrlCreateRequests")
    void 파일_업로드_URL_생성_요청값이_올바르지_않으면_400을_응답한다(UploadUrlCreateRequest request) {
        postJson(UPLOAD_URLS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(fileManager).shouldHaveNoInteractions();
    }

    @Test
    void 이미지_타입이_올바르지_않으면_400을_응답한다() {
        postJson(
                UPLOAD_URLS_URL,
                USER_ID,
                """
                {
                    "imageType": "INVALID",
                    "contentType": "image/jpeg",
                    "fileSize": 1024
                }
                """
        ).hasStatus(BAD_REQUEST);

        then(fileManager).shouldHaveNoInteractions();
    }

    @Test
    void 파일_크기가_허용량을_초과하면_400을_응답한다() {
        UploadUrlCreateRequest request = createUploadUrlCreateRequest(
                PROFILE_IMAGE,
                CONTENT_TYPE,
                PROFILE_IMAGE_MAX_SIZE_BYTES + 1
        );
        UploadUrlCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(FILE_SIZE_EXCEEDED)).given(fileManager).createUploadUrl(command);

        MvcTestResultAssert response = postJson(UPLOAD_URLS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(response, FILE_SIZE_EXCEEDED);
        then(fileManager).should().createUploadUrl(command);
    }

    @Test
    void 콘텐츠_타입이_허용되지_않으면_400을_응답한다() {
        UploadUrlCreateRequest request = createUploadUrlCreateRequest(PROFILE_IMAGE, "image/svg+xml", 1024L);
        UploadUrlCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(FILE_CONTENT_TYPE_NOT_ALLOWED))
                .given(fileManager)
                .createUploadUrl(command);

        MvcTestResultAssert response = postJson(UPLOAD_URLS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(response, FILE_CONTENT_TYPE_NOT_ALLOWED);
        then(fileManager).should().createUploadUrl(command);
    }

    private static Stream<UploadUrlCreateRequest> invalidUploadUrlCreateRequests() {
        return FileRequestFixture.invalidUploadUrlCreateRequests();
    }
}
