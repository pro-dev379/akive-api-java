package net.akive.api.file.application.required;

import net.akive.api.file.domain.UploadUrl;

import java.time.Duration;

public interface FileStorage {

    UploadUrl createUploadUrl(String fileKey, String contentType, long fileSize, Duration expiration);

    void delete(String fileKey);
}
