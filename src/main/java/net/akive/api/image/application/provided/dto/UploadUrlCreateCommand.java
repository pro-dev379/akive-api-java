package net.akive.api.image.application.provided.dto;

import net.akive.api.image.domain.ImageType;

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
