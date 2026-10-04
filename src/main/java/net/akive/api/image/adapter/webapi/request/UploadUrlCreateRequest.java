package net.akive.api.image.adapter.webapi.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import net.akive.api.image.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.image.domain.ImageType;

public record UploadUrlCreateRequest(
        @NotNull ImageType imageType,
        @NotBlank String contentType,
        @NotNull @Positive Long fileSize
) {

    public UploadUrlCreateCommand to(Long userId) {
        return new UploadUrlCreateCommand(userId, this.imageType, this.contentType, this.fileSize);
    }
}
