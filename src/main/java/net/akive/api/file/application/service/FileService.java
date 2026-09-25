package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.application.provided.FileManager;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.domain.UploadPurpose;
import net.akive.api.file.domain.UploadUrl;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.file.domain.FileUploadPolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.FileUploadPolicy.validateMaxFileSize;

@Service
@RequiredArgsConstructor
class FileService implements FileManager {

    private final FileStorage fileStorage;

    @Override
    public UploadUrlCreateResult createUploadUrl(UploadUrlCreateCommand command)
            throws BusinessRuleViolationException {
        validateMaxFileSize(command.uploadPurpose(), command.fileSize());

        String fileKey = createFileKey(command);

        UploadUrl uploadUrl = fileStorage.createUploadUrl(
                fileKey,
                command.contentType(),
                command.fileSize(),
                UPLOAD_URL_EXPIRATION_DURATION
        );

        return new UploadUrlCreateResult(fileKey, uploadUrl.value(), uploadUrl.expiresAt());
    }

    private String createFileKey(UploadUrlCreateCommand command) {
        String directory = switch (command.uploadPurpose()) {
            case PROFILE_IMAGE -> "profile-images";
            case ATTENDANCE_PROOF -> "attendance-proofs";
        };

        String extension = extensionOf(command.uploadPurpose(), command.contentType());

        return "%s/%d/%s.%s".formatted(
                directory,
                command.userId(),
                UUID.randomUUID(),
                extension
        );
    }

    private String extensionOf(UploadPurpose uploadPurpose, String contentType) {
        return switch (uploadPurpose) {
            case PROFILE_IMAGE, ATTENDANCE_PROOF -> switch (contentType) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> throw new BusinessRuleViolationException(FILE_CONTENT_TYPE_NOT_ALLOWED);
            };
        };
    }
}
