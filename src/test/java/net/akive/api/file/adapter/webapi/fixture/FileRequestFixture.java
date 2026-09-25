package net.akive.api.file.adapter.webapi.fixture;

import net.akive.api.file.adapter.webapi.request.UploadUrlCreateRequest;
import net.akive.api.file.domain.UploadPurpose;

import java.util.stream.Stream;

import static net.akive.api.file.application.fixture.FileCommandFixture.FILE_SIZE;
import static net.akive.api.file.domain.UploadPurpose.PROFILE_IMAGE;
import static net.akive.api.file.domain.fixture.FileFixture.CONTENT_TYPE;

public class FileRequestFixture {

    private FileRequestFixture() {
    }

    public static UploadUrlCreateRequest createUploadUrlCreateRequest() {
        return createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, FILE_SIZE);
    }

    public static UploadUrlCreateRequest createUploadUrlCreateRequest(
            UploadPurpose uploadPurpose,
            String contentType,
            Long fileSize
    ) {
        return new UploadUrlCreateRequest(uploadPurpose, contentType, fileSize);
    }

    public static Stream<UploadUrlCreateRequest> invalidUploadUrlCreateRequests() {
        return Stream.of(
                createUploadUrlCreateRequest(null, CONTENT_TYPE, FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, null, FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, "", FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, " ", FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, null),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, 0L),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, -1L)
        );
    }
}
