package net.akive.api.file.application.provided.dto;

import net.akive.api.file.domain.ImageType;

import static net.akive.api.common.domain.DomainValidator.*;

public record UploadUrlCreateCommand(
        Long userId,
        ImageType imageType,
        String contentType,
        Long fileSize
) {

    public UploadUrlCreateCommand {
        requirePositive(userId);
        requireNonNull(imageType);
        requireNotBlank(contentType);
        requirePositive(fileSize);
    }
}
