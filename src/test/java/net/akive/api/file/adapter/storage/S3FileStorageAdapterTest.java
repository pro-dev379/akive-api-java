package net.akive.api.file.adapter.storage;

import net.akive.api.core.config.aws.S3Properties;
import net.akive.api.file.domain.ImageType;
import net.akive.api.file.domain.UploadUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static net.akive.api.file.adapter.storage.fixture.S3FileStorageFixture.*;
import static net.akive.api.file.application.fixture.FileCommandFixture.FILE_SIZE;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
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

    @ParameterizedTest
    @CsvSource({
            "PROFILE_IMAGE, profile-images",
            "ATTENDANCE_PROOF, attendance-proofs"
    })
    void 원본_이미지_파일_정보를_서명하고_업로드_URL을_생성한다(ImageType imageType, String imageTypePath) throws Exception {
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedRequest);
        given(presignedRequest.url()).willReturn(UPLOAD_URL.toURL());
        given(presignedRequest.expiration()).willReturn(EXPIRES_AT);

        UploadUrl uploadUrl = fileStorage.createOriginalImageUploadUrl(
                IMAGE_ASSET_ID,
                imageType,
                "jpg",
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
        assertThat(objectRequest.key()).isEqualTo(
                "uploads/%s/%s/original.jpg".formatted(imageTypePath, IMAGE_ASSET_ID)
        );
        assertThat(objectRequest.contentType()).isEqualTo(CONTENT_TYPE);
        assertThat(objectRequest.contentLength()).isEqualTo(FILE_SIZE);
        assertThat(presignRequest.signatureDuration()).isEqualTo(UPLOAD_URL_EXPIRATION_DURATION);
        assertThat(uploadUrl.value()).isEqualTo(UPLOAD_URL);
        assertThat(uploadUrl.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void 스토리지에_원본_이미지_파일이_업로드되었는지_확인한다() {
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(FILE_KEY).size(FILE_SIZE).build())
                .build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);

        boolean uploaded = fileStorage.existsOriginalImage(IMAGE_ASSET_ID, PROFILE_IMAGE);

        ArgumentCaptor<ListObjectsV2Request> requestCaptor =
                ArgumentCaptor.forClass(ListObjectsV2Request.class);
        then(s3Client).should().listObjectsV2(requestCaptor.capture());

        ListObjectsV2Request request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.prefix()).isEqualTo(ORIGINAL_FILE_PREFIX);
        assertThat(request.maxKeys()).isEqualTo(1);
        assertThat(uploaded).isTrue();
    }

    @Test
    void 스토리지에_원본_이미지_파일이_없으면_원본_이미지가_업로드되지_않은_것으로_응답한다() {
        ListObjectsV2Response response = ListObjectsV2Response.builder().build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);

        boolean uploaded = fileStorage.existsOriginalImage(IMAGE_ASSET_ID, PROFILE_IMAGE);

        assertThat(uploaded).isFalse();
    }

    @Test
    void 스토리지에서_파일을_삭제한다() {
        fileStorage.delete(FILE_KEY);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        then(s3Client).should().deleteObject(requestCaptor.capture());

        DeleteObjectRequest deleteRequest = requestCaptor.getValue();
        assertThat(deleteRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(deleteRequest.key()).isEqualTo(FILE_KEY);
    }
}
