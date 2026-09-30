package net.akive.api.file.adapter.storage;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.S3Properties;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.domain.UploadUrl;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Component
@RequiredArgsConstructor
class S3FileStorageAdapter implements FileStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public UploadUrl createUploadUrl(String fileKey, String contentType, long fileSize, Duration expiration) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(fileKey)
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
    public void delete(String fileKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
