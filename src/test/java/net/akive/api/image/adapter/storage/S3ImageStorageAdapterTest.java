package net.akive.api.image.adapter.storage;

import net.akive.api.core.config.aws.S3Properties;
import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.UploadUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static net.akive.api.image.adapter.storage.fixture.S3ImageStorageFixture.*;
import static net.akive.api.image.application.fixture.ImageCommandFixture.IMAGE_FILE_SIZE;
import static net.akive.api.image.domain.ImagePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class S3ImageStorageAdapterTest {

    @Mock
    S3Client s3Client;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    PresignedPutObjectRequest presignedRequest;

    S3ImageStorageAdapter s3ImageStorageAdapter;

    @BeforeEach
    void setUp() {
        s3ImageStorageAdapter = new S3ImageStorageAdapter(
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

        UploadUrl uploadUrl = s3ImageStorageAdapter.createOriginalImageUploadUrl(
                IMAGE_ASSET_ID,
                imageType,
                "jpg",
                CONTENT_TYPE,
                IMAGE_FILE_SIZE,
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
        assertThat(objectRequest.contentLength()).isEqualTo(IMAGE_FILE_SIZE);
        assertThat(presignRequest.signatureDuration()).isEqualTo(UPLOAD_URL_EXPIRATION_DURATION);
        assertThat(uploadUrl.value()).isEqualTo(UPLOAD_URL);
        assertThat(uploadUrl.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void 스토리지에_원본_이미지_파일이_업로드되었는지_확인한다() {
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(S3Object.builder()
                        .key(ORIGINAL_IMAGE_OBJECT_KEY)
                        .size(IMAGE_FILE_SIZE)
                        .build())
                .build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);

        boolean uploaded = s3ImageStorageAdapter.existsOriginalImage(IMAGE_ASSET_ID, PROFILE_IMAGE);

        ArgumentCaptor<ListObjectsV2Request> requestCaptor =
                ArgumentCaptor.forClass(ListObjectsV2Request.class);
        then(s3Client).should().listObjectsV2(requestCaptor.capture());

        ListObjectsV2Request request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.prefix()).isEqualTo(ORIGINAL_IMAGE_OBJECT_PREFIX);
        assertThat(request.maxKeys()).isEqualTo(1);
        assertThat(uploaded).isTrue();
    }

    @Test
    void 스토리지에_원본_이미지_파일이_없으면_원본_이미지가_업로드되지_않은_것으로_응답한다() {
        ListObjectsV2Response response = ListObjectsV2Response.builder().build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);

        boolean uploaded = s3ImageStorageAdapter.existsOriginalImage(IMAGE_ASSET_ID, PROFILE_IMAGE);

        assertThat(uploaded).isFalse();
    }

    @Test
    void 스토리지에서_이미지_오브젝트를_삭제한다() {
        s3ImageStorageAdapter.delete(ORIGINAL_IMAGE_OBJECT_KEY);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        then(s3Client).should().deleteObject(requestCaptor.capture());

        DeleteObjectRequest deleteRequest = requestCaptor.getValue();
        assertThat(deleteRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(deleteRequest.key()).isEqualTo(ORIGINAL_IMAGE_OBJECT_KEY);
    }
}
