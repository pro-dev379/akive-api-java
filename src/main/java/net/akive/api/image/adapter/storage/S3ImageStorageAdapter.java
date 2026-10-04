package net.akive.api.image.adapter.storage;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.S3Properties;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.UploadUrl;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class S3ImageStorageAdapter implements ImageStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public UploadUrl createOriginalImageUploadUrl(
            UUID imageAssetId,
            ImageType imageType,
            String extension,
            String contentType,
            long fileSize,
            Duration expiration
    ) {
        String objectKey = createOriginalImageObjectKey(imageAssetId, imageType, extension);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(objectKey)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        URI uploadUrl = URI.create(presignedRequest.url().toExternalForm());

        return new UploadUrl(uploadUrl, presignedRequest.expiration());
    }

    @Override
    public boolean existsOriginalImage(UUID imageAssetId, ImageType imageType) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Properties.bucketName())
                .prefix(createOriginalImageObjectPrefix(imageAssetId, imageType))
                .maxKeys(1)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream().anyMatch(object -> object.size() > 0);
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private String createOriginalImageObjectKey(
            UUID imageAssetId,
            ImageType imageType,
            String extension
    ) {
        return createOriginalImageObjectPrefix(imageAssetId, imageType) + extension;
    }

    private String createOriginalImageObjectPrefix(UUID imageAssetId, ImageType imageType) {
        String imageTypePath = switch (imageType) {
            case PROFILE_IMAGE -> "profile-images";
            case ATTENDANCE_PROOF -> "attendance-proofs";
        };

        return "uploads/%s/%s/original.".formatted(imageTypePath, imageAssetId);
    }
}
