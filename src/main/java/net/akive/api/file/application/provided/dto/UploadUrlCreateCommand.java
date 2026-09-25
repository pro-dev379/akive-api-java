package net.akive.api.file.application.provided.dto;

import net.akive.api.file.domain.UploadPurpose;

import static net.akive.api.common.domain.DomainValidator.*;

public record UploadUrlCreateCommand(
        Long userId,
        UploadPurpose uploadPurpose,
        String contentType,
        Long fileSize
) {

    public UploadUrlCreateCommand {
        requirePositive(userId);
        requireNonNull(uploadPurpose);
        requireNotBlank(contentType);
        requirePositive(fileSize);
    }
}
