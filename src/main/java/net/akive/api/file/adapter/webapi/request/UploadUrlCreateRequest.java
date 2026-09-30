package net.akive.api.file.adapter.webapi.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.domain.ImageType;

public record UploadUrlCreateRequest(
        @NotNull ImageType imageType,
        @NotBlank String contentType,
        @NotNull @Positive Long fileSize
) {

    public UploadUrlCreateCommand to(Long userId) {
        return new UploadUrlCreateCommand(userId, this.imageType, this.contentType, this.fileSize);
    }
}
