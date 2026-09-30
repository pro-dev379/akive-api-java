package net.akive.api.file.adapter.storage;

import net.akive.api.core.config.aws.S3Properties;
import net.akive.api.file.domain.UploadUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static net.akive.api.file.adapter.storage.fixture.S3FileStorageFixture.*;
import static net.akive.api.file.application.fixture.FileCommandFixture.FILE_SIZE;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class S3FileStorageAdapterTest {

    @Mock
    S3Client s3Client;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    PresignedPutObjectRequest presignedRequest;

    S3FileStorageAdapter fileStorage;

    @BeforeEach
    void setUp() {
        fileStorage = new S3FileStorageAdapter(
                s3Client,
                s3Presigner,
                new S3Properties(BUCKET_NAME)
        );
    }

    @Test
    void 파일_정보를_서명하고_업로드_URL을_생성한다() throws Exception {
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);
        given(presignedRequest.url()).willReturn(UPLOAD_URL.toURL());
        given(presignedRequest.expiration()).willReturn(EXPIRES_AT);

        UploadUrl uploadUrl = fileStorage.createUploadUrl(
                FILE_KEY,
                CONTENT_TYPE,
                FILE_SIZE,
                UPLOAD_URL_EXPIRATION_DURATION
        );

        ArgumentCaptor<PutObjectPresignRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        then(s3Presigner).should().presignPutObject(requestCaptor.capture());

        PutObjectPresignRequest presignRequest = requestCaptor.getValue();
        PutObjectRequest objectRequest = presignRequest.putObjectRequest();
        assertThat(objectRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(objectRequest.key()).isEqualTo(FILE_KEY);
        assertThat(objectRequest.contentType()).isEqualTo(CONTENT_TYPE);
        assertThat(objectRequest.contentLength()).isEqualTo(FILE_SIZE);
        assertThat(presignRequest.signatureDuration()).isEqualTo(UPLOAD_URL_EXPIRATION_DURATION);
        assertThat(uploadUrl.value()).isEqualTo(UPLOAD_URL);
        assertThat(uploadUrl.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void 파일을_삭제한다() {
        fileStorage.delete(FILE_KEY);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        then(s3Client).should().deleteObject(requestCaptor.capture());

        DeleteObjectRequest deleteRequest = requestCaptor.getValue();
        assertThat(deleteRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(deleteRequest.key()).isEqualTo(FILE_KEY);
    }
}
