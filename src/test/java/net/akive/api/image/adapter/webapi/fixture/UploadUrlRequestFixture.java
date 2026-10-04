package net.akive.api.image.adapter.webapi.fixture;

import net.akive.api.image.adapter.webapi.request.UploadUrlCreateRequest;
import net.akive.api.image.domain.ImageType;

import java.util.stream.Stream;

import static net.akive.api.image.adapter.storage.fixture.S3ImageStorageFixture.CONTENT_TYPE;
import static net.akive.api.image.application.fixture.ImageCommandFixture.IMAGE_FILE_SIZE;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;

public class UploadUrlRequestFixture {

    private UploadUrlRequestFixture() {
    }

    public static UploadUrlCreateRequest createUploadUrlCreateRequest() {
        return createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, IMAGE_FILE_SIZE);
    }

    public static UploadUrlCreateRequest createUploadUrlCreateRequest(
            ImageType imageType,
            String contentType,
            Long fileSize
    ) {
        return new UploadUrlCreateRequest(imageType, contentType, fileSize);
    }

    public static Stream<UploadUrlCreateRequest> invalidUploadUrlCreateRequests() {
        return Stream.of(
                createUploadUrlCreateRequest(null, CONTENT_TYPE, IMAGE_FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, null, IMAGE_FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, "", IMAGE_FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, " ", IMAGE_FILE_SIZE),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, null),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, 0L),
                createUploadUrlCreateRequest(PROFILE_IMAGE, CONTENT_TYPE, -1L)
        );
    }
}
